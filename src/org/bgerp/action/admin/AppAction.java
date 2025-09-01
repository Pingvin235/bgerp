package org.bgerp.action.admin;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.dist.Maintenance;
import org.bgerp.app.dist.Scripts;
import org.bgerp.app.dist.inst.InstallerChanges;
import org.bgerp.app.dist.inst.InstallerChanges.Change;
import org.bgerp.app.dist.inst.VersionCheck;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.servlet.file.Files;
import org.bgerp.app.servlet.file.Highlighter;
import org.bgerp.app.servlet.file.Options;
import org.bgerp.app.servlet.file.Order;
import org.bgerp.app.servlet.user.LoginStat;
import org.bgerp.app.servlet.util.AccessLogValve;
import org.bgerp.util.Dynamic;
import org.bgerp.util.text.PatternFormatter;

import ru.bgcrm.dao.NewsDAO;
import ru.bgcrm.model.News;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path="/admin/app", pathId = true)
public class AppAction extends BaseAction {
    private static final String PATH_JSP = PATH_JSP_ADMIN + "/app";

    @Dynamic
    public static final Files LOG_APP = new Files(AppAction.class, "logApp", "log",
            new Options().withDownloadEnabled().withHighlighter(Highlighter.LOG_WARN).withDeletionEnabled().withOrder(Order.NORMAL_FS), "*bgerp*", "*mail*");
    @Dynamic
    public static final Files LOG_ACCESS = new Files(AppAction.class, "logAccess", AccessLogValve.DIR,
            new Options().withDownloadEnabled().withDeletionEnabled().withOrder(Order.LAST_MODIFIED_DESC), "*");
    @Dynamic
    public static final Files LOG_UPDATE = new Files(AppAction.class, "logUpdate", "log",
            new Options().withDownloadEnabled().withHighlighter(Highlighter.LOG_UPDATE_EXCEPTION).withDeletionEnabled().withOrder(Order.LAST_MODIFIED_DESC), "update_*");
    @Dynamic
    public static final Files UPDATE_ZIP = new Files(AppAction.class, "updateZip", Utils.getTmpDir(),
            new Options().withDownloadEnabled().withDeletionEnabled().withOrder(Order.LAST_MODIFIED_DESC), "update_*.zip");

    public ActionForward status(DynActionForm form, ConnectionSet conSet) throws Exception {
        if (VersionCheck.INSTANCE.isUpdateNeeded())
            form.setResponseData("error", l.l("App update is needed"));

        form.setResponseData("statusApp", AdminPortListener.statusApp());
        form.setResponseData("statusDb", Setup.getSetup().getConnectionPool().poolStatus());
        form.setResponseData("dbTrace", Setup.getSetup().getConnectionPool().getDbTrace());

        List<Change> changes = new InstallerChanges().getChanges();
        var masterRelease = changes.stream().filter(c -> InstallerChanges.MASTER_RELEASE_CHANGE_ID.equals(c.getId())).findAny().orElse(null);
        if (masterRelease != null)
            masterRelease.setTitle(masterRelease.getTitle().replaceFirst("0", "0 (" + l.l("Master Release") + ")"));

        form.setResponseData("changes", changes);

        return html(conSet, form, PATH_JSP + "/status.jsp");
    }

    public ActionForward downloadLogApp(DynActionForm form, ConnectionSet conSet) throws Exception {
        return LOG_APP.download(form);
    }

    public ActionForward highlightLogApp(DynActionForm form, ConnectionSet conSet) throws Exception {
        return json(conSet, LOG_APP.highlight(form));
    }

    public ActionForward deleteLogApp(DynActionForm form, ConnectionSet conSet) throws Exception {
        return json(conSet, LOG_APP.delete(form));
    }

    public ActionForward downloadLogAccess(DynActionForm form, ConnectionSet conSet) throws Exception {
        return LOG_ACCESS.download(form);
    }

    public ActionForward deleteLogAccess(DynActionForm form, ConnectionSet conSet) throws Exception {
        return json(conSet, LOG_ACCESS.delete(form));
    }

    public ActionForward downloadLogUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        return LOG_UPDATE.download(form);
    }

    public ActionForward highlightLogUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        return json(conSet, LOG_UPDATE.highlight(form));
    }

    public ActionForward deleteLogUpdate(DynActionForm form, ConnectionSet conSet) throws Exception {
        return json(conSet, LOG_UPDATE.delete(form));
    }

    public ActionForward downloadUpdateZip(DynActionForm form, ConnectionSet conSet) throws Exception {
        return UPDATE_ZIP.download(form);
    }

    public ActionForward deleteUpdateZip(DynActionForm form, ConnectionSet conSet) throws Exception {
        return json(conSet, UPDATE_ZIP.delete(form));
    }

    public ActionForward restart(DynActionForm form, ConnectionSet conSet) throws Exception {
        new Scripts().restart(form.getParamBoolean("force"));
        return json(conSet, form);
    }

    public ActionForward update(DynActionForm form, ConnectionSet conSet) throws Exception {
        new Scripts().backup(false).update(form.getParamBoolean("force"));
        return json(conSet, form);
    }

    public ActionForward updateToChange(DynActionForm form, ConnectionSet conSet) throws Exception {
        var changeId = form.getParam("changeId");
        if (Utils.isBlankString(changeId) || !NumberUtils.isDigits(changeId))
            throw new BGIllegalArgumentException();

        new Scripts().backup(false).installc(changeId);

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