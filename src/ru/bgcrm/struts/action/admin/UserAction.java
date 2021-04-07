package ru.bgcrm.struts.action.admin;

import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.dao.user.UserGroupDAO;
import ru.bgcrm.dao.user.UserPermsetDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.user.UserChangedEvent;
import ru.bgcrm.model.ArrayHashMap;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.model.user.Permset;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.PswdUtil;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;

public class UserAction extends ru.bgcrm.struts.action.BaseAction {
    
    public ActionForward permsetList(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        new UserPermsetDAO(con).searchPermset(new SearchResult<Permset>(form),
                CommonDAO.getLikePatternSub(form.getParam("filter")));

        return data(con, mapping, form, "permsetList");
    }

    public ActionForward permsetGet(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {

        Permset permset = new UserPermsetDAO(con).getPermsetById(form.getId());
        if (permset != null) {
            UserPermsetDAO permsetDao = new UserPermsetDAO(con);
            form.getResponse().setData("permset", permset);
            form.getResponse().setData("grantedPermission",
                    PermissionNode.addPermissionsSynonyms(permsetDao.getPermissions(permset.getId())));
        }

        form.getHttpRequest().setAttribute("allPermissions", UserCache.getAllPermTree());

        return data(con, mapping, form, "permsetUpdate");
    }

    public ActionForward permsetUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
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
        Set<String> config = form.getSelectedValuesStr("config");
        permsetDAO.updatePermissions(form.getSelectedValuesStr("dataPermissionType"), config, permset.getId());

        UserCache.flush(con);

        return status(con, form);
    }

    public ActionForward permsetDelete(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        new UserPermsetDAO(con).deletePermset(form.getId());

        UserCache.flush(con);

        return status(con, form);
    }

    public ActionForward permsetReplacePermissions(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        new UserPermsetDAO(con).replacePermissions(form.getParamInt("fromId"), form.getId());

        UserCache.flush(con);

        return status(con, form);
    }

    public ActionForward groupList(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        String filter = form.getParam("filter", "");

        int parentId = form.getParamInt("parentGroupId", 0);
        int archive = form.getParamInt("archive", 0);
        
        HttpServletRequest request = form.getHttpRequest(); 

        request.setAttribute("groupPath", UserCache.getGroupPath(parentId));

        new UserGroupDAO(con).searchGroup(new SearchResult<Group>(form), parentId, archive, filter);

        int id = form.getParamInt("markGroup", -1);
        if (id > 0) {
            Group group = new UserGroupDAO(con).getGroupById(id);
            if (group != null) {
                request.setAttribute("markGroupString", group.getTitle());
            }
        }

        return data(con, mapping, form, "groupList");
    }

    public ActionForward groupGet(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        UserGroupDAO groupDAO = new UserGroupDAO(con);

        Group group = groupDAO.getGroupById(form.getId());
        if (group != null) {
            group.setQueueIds(groupDAO.getGroupQueueIds(form.getId()));
            group.setPermsetIds(groupDAO.getGroupPermsetIds(form.getId()));

            form.getResponse().setData("group", group);
        }

        return data(con, mapping, form, "groupUpdate");
    }

    public ActionForward groupUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
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
        group.setQueueIds(form.getSelectedValues("queue"));
        group.setPermsetIds(form.getSelectedValuesList("permset"));
        group.setParentId(Utils.parseInt(form.getParam("parentGroupId")));
        group.setArchive((archive ? 1 : 0));
        group.setConfig(form.getParam("groupConfig"));

        if (Utils.isBlankString(group.getTitle())) {
            throw new BGMessageException("Не указано название.");
        }

        groupDAO.updateGroup(group);

        UserCache.flush(con);

        return status(con, form);
    }

    public ActionForward groupDelete(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {

        List<User> userInGroupList = new UserDAO(con).getUserList(Collections.singleton(form.getId()));

        if (!userInGroupList.isEmpty()) {
            throw new BGMessageException("Удаление группы невозможно. В группе содержатся пользователи.");
        }

        new UserGroupDAO(con).deleteGroup(form.getId());

        UserCache.flush(con);

        return status(con, form);
    }

    public ActionForward groupInsertMark(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
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

        return status(con, form);
    }

    // пользователи

    public ActionForward userList(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        Date actualDate = form.getParamDate("date", new Date());

        ParameterMap perm = form.getPermission();
        Set<Integer> groups = form.getSelectedValues("group");
        String allowOnlyGroups = perm.get("allowOnlyGroups", "");
        Set<Integer> permsets = form.getSelectedValues("permset");
        int status = form.getParamInt("status", 0);

        if (!(groups != null && groups.size() > 0) && Utils.notBlankString(allowOnlyGroups)) {
            groups = Utils.toIntegerSet(allowOnlyGroups);
        }

        new UserDAO(con).searchUser(new SearchResult<User>(form), CommonDAO.getLikePatternSub(form.getParam("title")),
                groups, null, actualDate, permsets, status);

        return data(con, mapping, form, "userList");
    }

    public ActionForward userGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        UserDAO userDAO = new UserDAO(con);

        User user = userDAO.getUser(form.getId());
        if (user != null) {
            form.getResponse().setData("user", user);
            form.getResponse().setData("grantedPermission",
                    PermissionNode.addPermissionsSynonyms(userDAO.getPermissions(user.getId())));
            form.getHttpRequest().setAttribute("userGroupList", userDAO.getUserGroupList(user.getId(), form.getParamDate("date")));
        }

        form.getHttpRequest().setAttribute("allPermissions", UserCache.getAllPermTree());

        return data(con, mapping, form, "userUpdate");
    }

    public ActionForward userUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        UserDAO userDAO = new UserDAO(con);

        int id = form.getId();

        User user = new User();
        if (id > 0) {
            user = userDAO.getUser(id);
        }

        if (user == null) {
            throw new BGMessageException("Пользователь не найден.");
        }

        ParameterMap perm = form.getPermission();

        user.setTitle(form.getParam("title"));
        user.setLogin(form.getParam("login"));
        user.setPassword(form.getParam("pswd", ""));
        user.setStatus(form.getParamInt("status", 0));
        //user.setEmail( form.getParam( "email" ) );
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
            user.setPermsetIds(form.getSelectedValuesList("permset"));
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
            user.setQueueIds(form.getSelectedValues("queue"));
        }

        if (Utils.isBlankString(user.getLogin())) {
            throw new BGMessageException("Не указан логин.");
        }
        User existingUser = userDAO.getUserByLogin(user.getLogin());
        if (existingUser != null && (id != existingUser.getId())) {
            throw new BGMessageException("Логин занят активным пользователем!");
        }
        if (Utils.isBlankString(user.getTitle())) {
            throw new BGMessageException("Не указано имя.");
        }

        new PswdUtil(setup, "user.").checkPassword(user.getPassword());

        userDAO.updateUser(user);

        // если юзер блокируется, то надо перенести всего его распределения в свободные
        /* if (user.getStatus() > 0) {
            AddressDistributionDAO distributionDAO = new AddressDistributionDAO(con);
            distributionDAO.freeUserHouses(user.getId());
        } */

        if (!perm.getBoolean("permDisable", false)) {
            userDAO.updatePermissions(form.getSelectedValuesStr("dataPermissionType"),
                    form.getSelectedValuesStr("config"), user.getId());
        }

        //PhoneProcessor.getProcessor().reloadUserPhones( con );

        UserCache.flush(con);

        EventProcessor.processEvent(new UserChangedEvent(form, user), new SingleConnectionConnectionSet(con));

        form.getResponse().setData("newUserId", user.getId());

        return status(con, form);
    }

    public ActionForward userDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        UserDAO userDAO = new UserDAO(con);
        userDAO.deleteUser(form.getId());

        // при удалении пользователя надо вернуть дома пользователя в нераспределенные
        /* AddressDistributionDAO distributionDAO = new AddressDistributionDAO(con);
        distributionDAO.freeUserHouses(form.getId()); */

        UserCache.flush(con);

        return status(con, form);
    }

    public ActionForward userGroupList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ParameterMap perm = form.getPermission();

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

        return data(con, mapping, form, "userGroupList");
    }

    public ActionForward userAddGroup(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Date fromDate = form.getParamDate("fromDate");
        Date toDate = form.getParamDate("toDate");

        int groupId = form.getParamInt("group");
        int userId = form.getParamInt("userId", 0);

        if (userId == 0) {
            userId = form.getId();
        }
        
        addGroup(form, con, fromDate, toDate, groupId, userId);

        return status(con, form);
    }

    public void addGroup(DynActionForm form, Connection con, Date fromDate, Date toDate, int groupId, int userId)
            throws BGException {
        if (groupId <= 0) {
            throw new BGException("Группа не указана!");
        }

        if (UserCache.getUserGroup(groupId) == null) {
            throw new BGException("Группа не найдена!");
        }

        ParameterMap perm = form.getPermission();

        String allowOnlyGroups = perm.get("allowOnlyGroups", "");

        if (Utils.notBlankString(allowOnlyGroups)) {
            Set<Integer> allowOnlyGroupsSet = Utils.toIntegerSet(allowOnlyGroups);

            if (!allowOnlyGroupsSet.contains(groupId)) {
                throw new BGException("Добавление этой группы запрещено!");
            }
        }

        UserGroup group = new UserGroup(groupId, fromDate, toDate);
        new UserDAO(con).addUserGroup(userId, group);

        UserCache.flush(con);
    }

    public ActionForward userRemoveGroup(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int userId = form.getParamInt("userId", -1);
        int groupId = form.getParamInt("groupId", -1);
        Date dateFrom = form.getParamDateTime("dateFrom");
        Date dateTo = form.getParamDateTime("dateTo");

        ParameterMap perm = form.getPermission();

        String allowOnlyGroups = perm.get("allowOnlyGroups", "");

        if (Utils.notBlankString(allowOnlyGroups)) {
            Set<Integer> allowOnlyGroupsSet = Utils.toIntegerSet(allowOnlyGroups);

            if (!allowOnlyGroupsSet.contains(groupId)) {
                throw new BGException("Удаление этой группы пользователя запрещено!");
            }
        }

        new UserDAO(con).removeUserGroup(userId, groupId, dateFrom, dateTo);
        UserCache.flush(con);

        return status(con, form);
    }

    public ActionForward userClosePeriodGroup(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int userId = form.getParamInt("userId", -1);
        int groupId = form.getParamInt("groupId", -1);
        final String maxTime = "23:59:59";
        Date date = TimeUtils.parse(form.getParam("date") + " " + maxTime, TimeUtils.PATTERN_DDMMYYYYHHMMSS);
        Date dateFrom = form.getParamDateTime("dateFrom");
        Date dateTo = form.getParamDateTime("dateTo");

        closeGroup(form, con, userId, groupId, date, dateFrom, dateTo);
        UserCache.flush(con);

        return status(con, form);
    }

    public void closeGroup(DynActionForm form, Connection con, int userId, int groupId, Date date, Date dateFrom,
            Date dateTo) throws BGException {
        if (date == null) {
            throw new BGException("Дата не должна быть пустой!");
        }

        ParameterMap perm = form.getPermission();
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
