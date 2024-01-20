package ru.bgcrm.struts.action.admin;

import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.dao.user.UserGroupDAO;
import ru.bgcrm.dao.user.UserPermsetDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.user.UserChangedEvent;
import ru.bgcrm.model.ArrayHashMap;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.model.user.Permset;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.PswdUtil.UserPswdUtil;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/admin/user")
public class UserAction extends org.bgerp.action.BaseAction {
    private static final String PATH_JSP = PATH_JSP_ADMIN + "/user";

    public ActionForward permsetList(DynActionForm form, Connection con) throws BGException {
        new UserPermsetDAO(con).searchPermset(new Pageable<Permset>(form),
                LikePattern.SUB.get(form.getParam("filter")));

        return html(con, form, PATH_JSP + "/permset/list.jsp");
    }

    public ActionForward permsetGet(DynActionForm form, Connection con) throws BGException {

        Permset permset = new UserPermsetDAO(con).getPermsetById(form.getId());
        if (permset != null) {
            UserPermsetDAO permsetDao = new UserPermsetDAO(con);
            form.getResponse().setData("permset", permset);
            form.getResponse().setData("grantedPermission",
                    PermissionNode.primaryActions(permsetDao.getPermissions(permset.getId())));
        }

        form.setRequestAttribute("permTrees", PermissionNode.getPermissionTrees());

        return html(con, form, PATH_JSP + "/permset/update.jsp");
    }

    public ActionForward permsetUpdate(DynActionForm form, Connection con) throws Exception {
        UserPermsetDAO permsetDAO = new UserPermsetDAO(con);

        int id = form.getId();

        Permset permset = new Permset();
        if (id > 0) {
            permset = permsetDAO.getPermsetById(id);
        }

        if (permset == null) {
            throw new BGMessageException("Группа не найдена.");
        }

        permset.setTitle(form.getParam("title", Utils::notBlankString));
        permset.setComment(form.getParam("comment", ""));
        permset.setConfig(form.getParam("permsetConfig", ""));

        permsetDAO.updatePermset(permset);
        Set<String> config = form.getParamValuesStr("permConfig");
        permsetDAO.updatePermissions(form.getParamValuesStr("permAction"), config, permset.getId());

        UserCache.flush(con);

        return json(con, form);
    }

    public ActionForward permsetDelete(DynActionForm form, Connection con) throws Exception {
        new UserPermsetDAO(con).deletePermset(form.getId());

        UserCache.flush(con);

        return json(con, form);
    }

    public ActionForward permsetReplacePermissions(DynActionForm form, Connection con) throws BGException {
        new UserPermsetDAO(con).replacePermissions(form.getParamInt("fromId"), form.getId());

        UserCache.flush(con);

        return json(con, form);
    }

    public ActionForward groupList(DynActionForm form, Connection con) throws BGException {
        String filter = form.getParam("filter", "");

        int parentId = form.getParamInt("parentGroupId", 0);
        int archive = form.getParamInt("archive", 0);

        HttpServletRequest request = form.getHttpRequest();

        request.setAttribute("groupPath", UserCache.getGroupPath(parentId));

        new UserGroupDAO(con).searchGroup(new Pageable<Group>(form), parentId, archive, filter);

        int id = form.getParamInt("markGroup", -1);
        if (id > 0) {
            Group group = new UserGroupDAO(con).getGroupById(id);
            if (group != null) {
                request.setAttribute("markGroupString", group.getTitle());
            }
        }

        return html(con, form, PATH_JSP + "/group/list.jsp");
    }

    public ActionForward groupGet(DynActionForm form, Connection con) throws Exception {
        UserGroupDAO groupDAO = new UserGroupDAO(con);

        Group group = groupDAO.getGroupById(form.getId());
        if (group != null) {
            group.setQueueIds(groupDAO.getGroupQueueIds(form.getId()));
            group.setPermsetIds(groupDAO.getGroupPermsetIds(form.getId()));

            form.getResponse().setData("group", group);
        }

        return html(con, form, PATH_JSP + "/group/update.jsp");
    }

    public ActionForward groupUpdate(DynActionForm form, Connection con) throws Exception {
        UserGroupDAO groupDAO = new UserGroupDAO(con);

        int id = form.getId();
        boolean archive = Utils.parseBoolean(form.getParam("archive"), false);

        Group group = new Group();
        if (id > 0) {
            group = groupDAO.getGroupById(id);
        }

        if (group == null) {
            throw new BGMessageException("Группа не найдена.");
        }

        group.setTitle(form.getParam("title").replace("\"", ""));
        group.setComment(form.getParam("comment"));
        group.setQueueIds(form.getParamValues("queue"));
        group.setPermsetIds(form.getParamValuesList("permset"));
        group.setParentId(Utils.parseInt(form.getParam("parentGroupId")));
        group.setArchive((archive ? 1 : 0));
        group.setConfig(form.getParam("groupConfig"));

        if (Utils.isBlankString(group.getTitle())) {
            throw new BGMessageException("Не указано название.");
        }

        groupDAO.updateGroup(group);

        UserCache.flush(con);

        return json(con, form);
    }

    public ActionForward groupDelete(DynActionForm form, Connection con) throws Exception {

        List<User> userInGroupList = new UserDAO(con).getUserList(Collections.singleton(form.getId()));

        if (!userInGroupList.isEmpty()) {
            throw new BGMessageException("Удаление группы невозможно. В группе содержатся пользователи.");
        }

        new UserGroupDAO(con).deleteGroup(form.getId());

        UserCache.flush(con);

        return json(con, form);
    }

    public ActionForward groupInsertMark(DynActionForm form, Connection con) throws Exception {
        ArrayHashMap paramMap = form.getParam();
        int parentId = Utils.parseInt(paramMap.get("parentGroupId"), 0);
        int id = Utils.parseInt(paramMap.get("markGroup"), -1);

        if (id != -1) {
            UserGroupDAO groupDAO = new UserGroupDAO(con);
            Group group = groupDAO.getGroupById(id);

            if (!groupDAO.checkGroup(0, parentId, group.getTitle())) {
                throw new BGMessageException("Такое имя уже существует в данной ветке.");
            }

            if (parentId == id) {
                throw new BGMessageException("Нельзя копировать в самого себя.");
            }

            group.setParentId(parentId);
            groupDAO.updateGroup(group);
            UserCache.flush(con);
            paramMap.put("markGroup", "0");
        }

        return json(con, form);
    }

    public ActionForward userList(DynActionForm form, Connection con) throws Exception {
        Date actualDate = form.getParamDate("date", new Date());

        ConfigMap perm = form.getPermission();
        Set<Integer> groups = form.getParamValues("group");
        String allowOnlyGroups = perm.get("allowOnlyGroups", "");
        Set<Integer> permsets = form.getParamValues("permset");
        int status = form.getParamInt("status", 0);

        if (!(groups != null && groups.size() > 0) && Utils.notBlankString(allowOnlyGroups)) {
            groups = Utils.toIntegerSet(allowOnlyGroups);
        }

        new UserDAO(con).searchUser(new Pageable<>(form), LikePattern.SUB.get(form.getParam("title")),
                groups, null, actualDate, permsets, status);

        return html(con, form, PATH_JSP + "/user/list.jsp");
    }

    public ActionForward userGet(DynActionForm form, Connection con) throws Exception {
        UserDAO userDAO = new UserDAO(con);

        User user = userDAO.getUser(form.getId());
        if (user != null) {
            form.setResponseData("user", user);
            form.setResponseData("grantedPermission",
                    PermissionNode.primaryActions(userDAO.getPermissions(user.getId())));
            form.setRequestAttribute("userGroupList", userDAO.getUserGroupList(user.getId(), form.getParamDate("date")));
        }

        form.setRequestAttribute("permTrees", PermissionNode.getPermissionTrees());

        return html(con, form, PATH_JSP + "/user/update.jsp");
    }

    public ActionForward userUpdate(DynActionForm form, Connection con) throws Exception {
        UserDAO userDAO = new UserDAO(con);

        int id = form.getId();

        User user = new User();
        if (id > 0) {
            user = userDAO.getUser(id);
            if (user == null) {
                throw new BGMessageException("Пользователь не найден.");
            }
        }

        ConfigMap perm = form.getPermission();

        user.setTitle(form.getParam("title", Utils::notBlankString));
        user.setLogin(form.getParam("login", Utils::notBlankString));
        user.setPassword(form.getParam("pswd", ""));
        user.setStatus(form.getParamInt("status", 0));

        if (!perm.getBoolean("configDisable", false)) {
            user.setConfig(form.getParam("userConfig", ""));
        }
        user.setDescription(form.getParam("description", ""));

        String setConfVars = perm.get("setConfigVars");
        if (Utils.notBlankString(setConfVars)) {
            user.setConfig(user.getConfig() + "\n" + setConfVars.replace(';', '\n'));
        }

        String permsetSet = perm.get("permsetSet", "");
        if (Utils.notBlankString(permsetSet)) {
            if (id <= 0) {
                user.setPermsetIds(Utils.toIntegerList(permsetSet));
            }
        } else {
            user.setPermsetIds(form.getParamValuesList("permset"));
        }

        String groupSet = perm.get("groupSet", "");

        if (Utils.notBlankString(groupSet)) {
            if (id <= 0) {
                user.setGroupIds(Utils.toIntegerSet(groupSet));
            }
        }

        String queueSet = perm.get("queueSet", "");
        if (Utils.notBlankString(queueSet)) {
            if (id <= 0) {
                user.setQueueIds(Utils.toIntegerSet(queueSet));
            }
        } else {
            user.setQueueIds(form.getParamValues("queue"));
        }

        User existingUser = userDAO.getUserByLogin(user.getLogin());
        if (existingUser != null && (id != existingUser.getId())) {
            throw new BGMessageException("Логин занят активным пользователем!");
        }

        new UserPswdUtil(setup).checkPassword(user.getPassword());

        userDAO.updateUser(user);

        if (!perm.getBoolean("permDisable", false)) {
            userDAO.updatePermissions(form.getParamValuesStr("permAction"),
                    form.getParamValuesStr("permConfig"), user.getId());
        }

        UserCache.flush(con);

        EventProcessor.processEvent(new UserChangedEvent(form, user), new SingleConnectionSet(con));

        form.setResponseData("newUserId", user.getId());

        return json(con, form);
    }

    public ActionForward userDelete(DynActionForm form, Connection con) throws Exception {
        UserDAO userDAO = new UserDAO(con);
        userDAO.deleteUser(form.getId());

        UserCache.flush(con);

        return json(con, form);
    }

    public ActionForward userGroupList(DynActionForm form, Connection con) throws Exception {
        ConfigMap perm = form.getPermission();

        String allowOnlyGroups = perm.get("allowOnlyGroups", "");

        if (Utils.notBlankString(allowOnlyGroups)) {
            boolean contains = false;
            Set<Integer> allowOnlyGroupsSet = Utils.toIntegerSet(allowOnlyGroups);

            for (Integer allowGroup : allowOnlyGroupsSet) {
                if (UserCache.getUser(form.getId()).getGroupIds().contains(allowGroup)) {
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                throw new BGException("Просмотр групп этого пользователя запрещен!");
            }
        }

        form.getHttpRequest().setAttribute("userGroupList", UserCache.getUserGroupList(form.getId(), form.getParamDate("date")));

        return html(con, form, PATH_JSP + "/user/update_usergroup.jsp");
    }

    public ActionForward userAddGroup(DynActionForm form, Connection con) throws Exception {
        Date fromDate = form.getParamDate("fromDate");
        Date toDate = form.getParamDate("toDate");

        int groupId = form.getParamInt("group");
        int userId = form.getParamInt("userId", 0);

        if (userId == 0) {
            userId = form.getId();
        }

        addGroup(form, con, fromDate, toDate, groupId, userId);

        return json(con, form);
    }

    public void addGroup(DynActionForm form, Connection con, Date fromDate, Date toDate, int groupId, int userId)
            throws Exception {
        if (groupId <= 0) {
            throw new BGMessageException("Группа не указана!");
        }

        if (UserCache.getUserGroup(groupId) == null) {
            throw new BGMessageException("Группа не найдена!");
        }

        ConfigMap perm = form.getPermission();

        String allowOnlyGroups = perm.get("allowOnlyGroups", "");

        if (Utils.notBlankString(allowOnlyGroups)) {
            Set<Integer> allowOnlyGroupsSet = Utils.toIntegerSet(allowOnlyGroups);

            if (!allowOnlyGroupsSet.contains(groupId)) {
                throw new BGMessageException("Добавление этой группы запрещено!");
            }
        }

        UserGroup group = new UserGroup(groupId, fromDate, toDate);
        new UserDAO(con).addUserGroup(userId, group);

        UserCache.flush(con);
    }

    public ActionForward userRemoveGroup(DynActionForm form, Connection con) throws Exception {
        int userId = form.getParamInt("userId", -1);
        int groupId = form.getParamInt("groupId", -1);
        Date dateFrom = form.getParamDate("dateFrom");
        Date dateTo = form.getParamDate("dateTo");

        ConfigMap perm = form.getPermission();

        String allowOnlyGroups = perm.get("allowOnlyGroups", "");

        if (Utils.notBlankString(allowOnlyGroups)) {
            Set<Integer> allowOnlyGroupsSet = Utils.toIntegerSet(allowOnlyGroups);
            if (!allowOnlyGroupsSet.contains(groupId)) {
                throw new BGMessageException("Удаление этой группы пользователя запрещено!");
            }
        }

        new UserDAO(con).removeUserGroup(userId, groupId, dateFrom, dateTo);
        UserCache.flush(con);

        return json(con, form);
    }

    public ActionForward userClosePeriodGroup(DynActionForm form, Connection con) throws Exception {
        int userId = form.getParamInt("userId", -1);
        int groupId = form.getParamInt("groupId", -1);
        Date date = form.getParamDate("date");
        Date dateFrom = form.getParamDate("dateFrom");
        Date dateTo = form.getParamDate("dateTo");

        closeGroup(form, con, userId, groupId, date, dateFrom, dateTo);
        UserCache.flush(con);

        return json(con, form);
    }

    public void closeGroup(DynActionForm form, Connection con, int userId, int groupId, Date date, Date dateFrom,
            Date dateTo) throws Exception {
        if (date == null) {
            throw new BGException("Дата не должна быть пустой!");
        }

        ConfigMap perm = form.getPermission();
        String allowOnlyGroups = perm.get("allowOnlyGroups", "");

        if (Utils.notBlankString(allowOnlyGroups)) {
            Set<Integer> allowOnlyGroupsSet = Utils.toIntegerSet(allowOnlyGroups);

            if (!allowOnlyGroupsSet.contains(groupId)) {
                throw new BGException("Закрытие периода для этой группы пользователя запрещено!");
            }
        }

        new UserDAO(con).closeUserGroupPeriod(userId, groupId, date, dateFrom, dateTo);
    }

}
