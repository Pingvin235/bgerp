package org.bgerp.action.admin;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.servlet.file.Files;
import org.bgerp.servlet.file.Options;
import org.bgerp.servlet.file.Order;
import org.bgerp.servlet.user.LoginStat;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.distr.Scripts;
import ru.bgcrm.util.distr.UpdateProcessor;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path="/admin/app")
public class AppAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_ADMIN + "/app";

    /** Accessed from JSP. */
    public static final Files LOG_APP = new Files(AppAction.class, "logApp", "log", "bgerp.*",
            new Options().withDownloadEnabled().withOrder(Order.LAST_MODIFIED_DESC));
    public static final Files LOG_UPDATE = new Files(AppAction.class, "logUpdate", "log", "update_*",
            new Options().withDownloadEnabled().withDeletionEnabled().withOrder(Order.LAST_MODIFIED_DESC));
    public static final Files UPDATE_ZIP = new Files(AppAction.class, "updateZip", ".", "update_*.zip",
            new Options().withDownloadEnabled().withDeletionEnabled().withOrder(Order.LAST_MODIFIED_DESC));

    public ActionForward status(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setResponseData("status", AdminPortListener.getStatus());
        form.setResponseData("changes", new UpdateProcessor().getChanges());

        return html(conSet, form, PATH_JSP + "/status.jsp");
    }

    public ActionForward downloadLogApp(DynActionForm form, ConnectionSet conSet) throws Exception {
        return LOG_APP.download(form);
    }

    public ActionForward downloadLogUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        return LOG_UPDATE.download(form);
    }

    public ActionForward deleteLogUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        LOG_UPDATE.delete(form);
        return json(conSet, form);
    }

    public ActionForward downloadUpdateZip(DynActionForm form, ConnectionSet conSet) throws Exception {
        return UPDATE_ZIP.download(form);
    }

    public ActionForward deleteUpdateZip(DynActionForm form, ConnectionSet conSet) throws Exception {
        UPDATE_ZIP.delete(form);
        return json(conSet, form);
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
        return html(conSet, form, PATH_JSP + "/user_logged_list.jsp");
    }

}