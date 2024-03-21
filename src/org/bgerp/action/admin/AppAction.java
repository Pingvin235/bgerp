package org.bgerp.action.admin;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.dist.Maintenance;
import org.bgerp.app.dist.Scripts;
import org.bgerp.app.dist.inst.InstallerChanges;
import org.bgerp.app.dist.inst.InstallerChanges.Change;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.dist.inst.VersionCheck;
import org.bgerp.app.servlet.file.Files;
import org.bgerp.app.servlet.file.Options;
import org.bgerp.app.servlet.file.Order;
import org.bgerp.app.servlet.user.LoginStat;
import org.bgerp.util.Dynamic;

import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.model.News;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.AccessLogValve;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.PatternFormatter;
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

    public ActionForward maintenance(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setResponseData("logged", LoginStat.instance().loggedUsersWithSessions());
        Maintenance maintenance = Maintenance.instance();
        if (maintenance != null) {
            form.setResponseData("maintenance", maintenance);
            form.setResponseData("maintenanceState",
                l.l("Maintenance is running by {} since {}. User's logoff time is {}.",
                    maintenance.getUser().getTitle(),
                    maintenance.getStartTime().format(DateTimeFormatter.ISO_LOCAL_TIME),
                    maintenance.getLogoffTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
                )
            );
        }
        return html(conSet, form, PATH_JSP + "/maintenance.jsp");
    }

    public ActionForward maintenanceStart(DynActionForm form, ConnectionSet conSet) throws Exception {
        Duration logoffDelay = Duration.ofMinutes(form.getParamInt("delayMinutes", 5));

        var maintenance = Maintenance.start(form.getUser(), logoffDelay);

        String message = form.getParam("message");
        if (!Utils.isBlankString(message)) {
            var dao = new NewsDAO(conSet.getConnection());

            News news = new News();
            news.setUserId(form.getUserId());
            news.setTitle(l.l("Maintenance"));
            news.setDescription(PatternFormatter.processPattern(message, Map.of(
                "user", maintenance.getUser().getTitle(),
                "time", maintenance.getLogoffTime().format(DateTimeFormatter.ISO_LOCAL_TIME)
            )));
            news.setPopup(true);
            news.setReadTime(1);

            dao.updateNewsUsers(news, LoginStat.instance().loggedUsers().stream().map(User::getId).collect(Collectors.toSet()));
        }

        return json(conSet, form);
    }

    public ActionForward maintenanceCancel(DynActionForm form, ConnectionSet conSet) throws Exception {
        var maintenance = Maintenance.cancel();

        var dao = new NewsDAO(conSet.getConnection());

        News news = new News();
        news.setUserId(form.getUserId());
        news.setTitle(l.l("Maintenance cancel"));
        news.setDescription(l.l("maintenance.cancel", maintenance.getStartTime().format(DateTimeFormatter.ISO_LOCAL_TIME)));
        news.setPopup(true);
        news.setReadTime(1);

        dao.updateNewsUsers(news, LoginStat.instance().loggedUsers().stream().map(User::getId).collect(Collectors.toSet()));

        return json(conSet, form);
    }
}