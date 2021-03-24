package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.PswdUtil;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProfileAction extends BaseAction {
    
    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        return getUserProfile(mapping, form, conSet);
    }

    public ActionForward getUserProfile(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        return data(conSet, mapping, form, "profile");
    }

    public ActionForward settings(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
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
            return data(con, mapping, form, "parameters");
        }

        return data(con, mapping, form, "settings");
    }

    public ActionForward updateSettings(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
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

        new PswdUtil(setup, "user.").checkPassword(userPassword);

        user.setTitle(userName);
        user.setLogin(userLogin);
        user.setPassword(userPassword);
        user.setDescription(form.getParam("userDescription"));

        userDAO.updateUser(user);

        UserCache.flush(con);

        return status(con, form);
    }

    public ActionForward updatePersonalization(ActionMapping mapping, DynActionForm form, Connection con)
            throws Exception {
        User user = UserCache.getUser(form.getUserId());
        UserDAO userDAO = new UserDAO(con);

        // TODO: Ovewrite mode has not implemented yet.
        Preferences personalizationMap = form.getParamBoolean("overwrite") ? new Preferences() : user.getPersonalizationMap();
        String persConfigBefore = personalizationMap.getDataString();

        for (Map.Entry<String, String[]> me : form.getHttpRequest().getParameterMap().entrySet()) {
            String key = me.getKey();
            String value = me.getValue()[0];
            if (!key.startsWith("iface."))
                continue;
            personalizationMap.put(key.replace('_', '.'), value);
        }

        userDAO.updatePersonalization(persConfigBefore, user);

        UserCache.flush(con);

        return status(con, form);
    }
    
}