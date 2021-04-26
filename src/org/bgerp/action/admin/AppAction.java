package org.bgerp.action.admin;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.util.Files;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.servlet.LoginStat;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.distr.Scripts;
import ru.bgcrm.util.distr.UpdateProcessor;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path="/admin/app")
public class AppAction extends BaseAction {
    private static final String JSP_PATH = PATH_JSP_ADMIN + "/app";

    /** Accessed from JSP. */
    public static final Files LOG_APP = new Files(AppAction.class, "logApp", "log", "bgerp.*");
    public static final Files LOG_UPDATE = new Files(AppAction.class, "logUpdate", "log", "update_*");

    public ActionForward status(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setResponseData("status", AdminPortListener.getStatus());
        form.setResponseData("changeIds", new UpdateProcessor().getChangeIds());

        return html(conSet, form, JSP_PATH + "/status.jsp");
    }

    public ActionForward downloadLogApp(DynActionForm form, ConnectionSet conSet) throws Exception {
        return LOG_APP.download(form);
    }

    public ActionForward downloadLogUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        return LOG_UPDATE.download(form);
    }

    public ActionForward restart(DynActionForm form, ConnectionSet conSet) throws Exception {
        new Scripts().restart(form.getParamBoolean("force"));
        return json(conSet, form);
    }

    public ActionForward update(DynActionForm form, ConnectionSet conSet) throws Exception {
        new Scripts().backup(false).update(form.getParamBoolean("force")).restart(form.getParamBoolean("restartForce"));
        return json(conSet, form);
    }
    
    public ActionForward updateToChange(DynActionForm form, ConnectionSet conSet) throws Exception {
        var changeId = form.getParam("changeId");
        if (Utils.isBlankString(changeId) || !NumberUtils.isDigits(changeId))
            throw new BGIllegalArgumentException();

        var files = new UpdateProcessor(changeId).getUpdateFiles();

        if (files.isEmpty())
            throw new BGMessageException("Не найдены файлы обновлений.");

        new Scripts().backup(false).install(files).restart(form.getParamBoolean("restartForce"));

        return json(conSet, form);
    }

    public ActionForward userLoggedList(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setResponseData("logged", LoginStat.getLoginStat().getLoggedUserWithSessions());
        return html(conSet, form, JSP_PATH + "/user_logged_list.jsp");
    }

}