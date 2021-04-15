package ru.bgcrm.struts.action.admin;

import java.io.File;
import java.io.FileFilter;

import com.google.common.collect.Lists;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.servlet.LoginStat;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.distr.Scripts;
import ru.bgcrm.util.distr.UpdateProcessor;
import ru.bgcrm.util.sql.ConnectionSet;

public class AppAction extends BaseAction {
    private static final String JSP_PATH = PATH_JSP_ADMIN + "/app";

    public ActionForward status(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setResponseData("status", AdminPortListener.getStatus());

        form.setResponseData("changeIds", new UpdateProcessor().getChangeIds());

        FileFilter fileFilter = new WildcardFileFilter("log_update*");
        var logFiles = Lists.newArrayList(new File(".").listFiles(fileFilter)) ;
        logFiles.sort((f1, f2) -> (int) (f2.lastModified() - f1.lastModified()));

        form.setResponseData("logUpdateList", logFiles);

        return html(conSet, form, JSP_PATH + "/status.jsp");
    }

    public ActionForward restart(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Scripts.restart();
        return json(conSet, form);
    }

    public ActionForward update(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Scripts.backupUpdateRestart(form.getParamBoolean("force"));
        return json(conSet, form);
    }
    
    public ActionForward updateToChange(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        var changeId = form.getParam("changeId");
        if (Utils.isBlankString(changeId) || !NumberUtils.isDigits(changeId))
            throw new BGIllegalArgumentException();

        var updateFiles = new UpdateProcessor(changeId).getUpdateFiles();

        if (updateFiles.isEmpty())
            throw new BGMessageException("Не найдены файлы обновлений.");

        Scripts.backupInstallRestart(updateFiles);

        return json(conSet, form);
    }

    public ActionForward userLoggedList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setResponseData("logged", LoginStat.getLoginStat().getLoggedUserWithSessions());
        return html(conSet, form, JSP_PATH + "/user_logged_list.jsp");
    }

}