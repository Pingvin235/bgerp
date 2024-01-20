package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.app.cfg.Preferences;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.PswdUtil.UserPswdUtil;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/profile")
public class ProfileAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/profile";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return getUserProfile(form, conSet);
    }

    public ActionForward getUserProfile(DynActionForm form, ConnectionSet conSet) throws Exception {
        return html(conSet, form, PATH_JSP + "/default.jsp");
    }

    public ActionForward settings(DynActionForm form, Connection con) throws Exception {
        int requestedUserId = form.getParamInt("requestUserId", 0);

        UserDAO userDAO = new UserDAO(con);
        User user = UserCache.getUser(requestedUserId > 0 ? requestedUserId : form.getUserId());
        String subAction = form.getParam("subAction") == null ? "" : form.getParam("subAction");

        if (user != null) {
            form.getResponse().setData("user", user);
            form.getResponse().setData("userGroupList", UserCache.getUserGroupList(user.getId(), new Date()));
            form.getResponse().setData("grantedPermission", userDAO.getPermissions(user.getId()));
        }

        if (subAction.equals("parameters")) {
            return html(con, form, PATH_JSP + "/parameters.jsp");
        }

        return html(con, form, PATH_JSP + "/settings.jsp");
    }

    public ActionForward updateSettings(DynActionForm form, Connection con) throws Exception {
        User user = UserCache.getUser(form.getUserId());
        UserDAO userDAO = new UserDAO(con);

        String userName = form.getParam("userName");
        String userLogin = form.getParam("userLogin");
        String userPassword = form.getParam("userPassword", "");

        if (Utils.isBlankString(userName)) {
            throw new BGMessageException("Не указан логин.");
        }
        if (userDAO.getUserByLogin(userLogin) != null) {
            if (userDAO.getUserByLogin(userLogin).getId() != form.getUserId()) {
                throw new BGMessageException("Пользователь с данным логином уже существует,");
            }
        }
        if (Utils.isBlankString(userName)) {
            throw new BGMessageException("Не указано имя.");
        }

        new UserPswdUtil(setup).checkPassword(userPassword);

        user.setTitle(userName);
        user.setLogin(userLogin);
        user.setPassword(userPassword);
        user.setDescription(form.getParam("userDescription"));

        userDAO.updateUser(user);

        UserCache.flush(con);

        return json(con, form);
    }

    public ActionForward updatePersonalization(DynActionForm form, Connection con) throws Exception {
        User user = UserCache.getUser(form.getUserId());
        UserDAO userDAO = new UserDAO(con);

        Preferences map = user.getPersonalizationMap();
        String mapDataBefore = map.getDataString();

        if (form.getParamBoolean("overwrite"))
            map.clear();

        for (Map.Entry<String, String[]> me : form.getHttpRequest().getParameterMap().entrySet()) {
            String key = me.getKey();
            String value = me.getValue()[0];
            if (!key.startsWith("iface."))
                continue;
            map.put(key.replace('_', '.'), value);
        }

        if (form.getParamBoolean("reset"))
            map.clear();

        userDAO.updatePersonalization(mapDataBefore, user);

        UserCache.flush(con);

        return json(con, form);
    }
}