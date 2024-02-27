package org.bgerp.action.admin;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.dist.Scripts;
import org.bgerp.app.dist.inst.InstallerChanges;
import org.bgerp.app.dist.inst.InstallerChanges.Change;
import org.bgerp.app.dist.inst.VersionCheck;
import org.bgerp.app.servlet.file.Files;
import org.bgerp.app.servlet.file.Options;
import org.bgerp.app.servlet.file.Order;
import org.bgerp.app.servlet.user.LoginStat;
import org.bgerp.util.Dynamic;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.servlet.AccessLogValve;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path="/admin/app")
public class AppAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_ADMIN + "/app";

    @Dynamic
    public static final Files LOG_APP = new Files(AppAction.class, "logApp", "log", "bgerp*",
            new Options().withDownloadEnabled().withDeletionEnabled().withOrder(Order.LAST_MODIFIED_DESC));
    @Dynamic
    public static final Files LOG_ACCESS = new Files(AppAction.class, "logAccess", AccessLogValve.DIR, "*",
            new Options().withDownloadEnabled().withDeletionEnabled().withOrder(Order.LAST_MODIFIED_DESC));
    @Dynamic
    public static final Files LOG_UPDATE = new Files(AppAction.class, "logUpdate", "log", "update_*",
            new Options().withDownloadEnabled().withDeletionEnabled().withOrder(Order.LAST_MODIFIED_DESC));
    @Dynamic
    public static final Files UPDATE_ZIP = new Files(AppAction.class, "updateZip", Utils.getTmpDir(), "update_*.zip",
            new Options().withDownloadEnabled().withDeletionEnabled().withOrder(Order.LAST_MODIFIED_DESC));

    public ActionForward status(DynActionForm form, ConnectionSet conSet) throws Exception {
        if (VersionCheck.INSTANCE.isUpdateNeeded())
            form.setResponseData("error", l.l("App update is needed"));

        form.setResponseData("statusApp", AdminPortListener.statusApp());
        form.setResponseData("statusDb", Setup.getSetup().getConnectionPool().poolStatus());
        form.setResponseData("dbTrace", Setup.getSetup().getConnectionPool().getDbTrace());

        List<Change> changes = new InstallerChanges().getChanges();
        var preStableRelease = changes.stream().filter(c -> InstallerChanges.PRE_RELEASE_CHANGE_ID.equals(c.getId())).findAny();
        if (preStableRelease.isPresent())
            preStableRelease.get().setTitle("0 (" + l.l("Pre-Stable") + ")");

        form.setResponseData("changes", changes);

        return html(conSet, form, PATH_JSP + "/status.jsp");
    }

    public ActionForward downloadLogApp(DynActionForm form, ConnectionSet conSet) throws Exception {
        return LOG_APP.download(form);
    }

    public ActionForward deleteLogApp(DynActionForm form, ConnectionSet conSet) throws Exception {
        LOG_APP.delete(form);
        return json(conSet, form);
    }

    public ActionForward downloadLogAccess(DynActionForm form, ConnectionSet conSet) throws Exception {
        return LOG_ACCESS.download(form);
    }

    public ActionForward deleteLogAccess(DynActionForm form, ConnectionSet conSet) throws Exception {
    	LOG_ACCESS.delete(form);
        return json(conSet, form);
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
        new Scripts().restart(form.getParamBoolean("restartForce"));
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

        new Scripts().backup(false).installc(changeId).restart(form.getParamBoolean("restartForce"));

        return json(conSet, form);
    }

    public ActionForward userLoggedList(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setResponseData("logged", LoginStat.getLoginStat().getLoggedUserWithSessions());
        return html(conSet, form, PATH_JSP + "/user_logged_list.jsp");
    }

}