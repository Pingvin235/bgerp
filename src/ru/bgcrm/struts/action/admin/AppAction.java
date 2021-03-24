package ru.bgcrm.struts.action.admin;

import static ru.bgcrm.util.distr.InstallProcessor.UPDATE_TO_CHANGE_URL;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.servlet.LoginStat;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AdminPortListener;
import ru.bgcrm.util.distr.Scripts;
import ru.bgcrm.util.sql.ConnectionSet;

public class AppAction extends BaseAction {
    private static final String JSP_PATH = PATH_JSP_ADMIN + "/app";

    public ActionForward status(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setResponseData("status", AdminPortListener.getStatus());

        FileFilter fileFilter = new WildcardFileFilter("log_update*");
        List<File> logFiles = Lists.newArrayList(new File(".").listFiles(fileFilter)) ;
        logFiles.sort((f1, f2) -> (int) (f2.lastModified() - f1.lastModified()));

        form.setResponseData("logUpdateList", logFiles);

        return data(conSet, form, JSP_PATH + "/status.jsp");
    }

    public ActionForward restart(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Scripts.restart();
        return status(conSet, form);
    }

    public ActionForward update(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Scripts.backupUpdateRestart(form.getParamBoolean("force"));
        return status(conSet, form);
    }
    
    public ActionForward updateToChange(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        int processId = form.getParamInt("processId");
        if (processId < 0)
            throw new BGIllegalArgumentException();

        List<String> updateFiles = new ArrayList<>(2);

        final String changeFolder = UPDATE_TO_CHANGE_URL + processId;
        Document doc = Jsoup.connect(changeFolder).get();
        for (Element link : doc.select("a")) {
            String href = link.attr("href");
            if (href.endsWith(".zip") && href.startsWith("update_") || href.startsWith("update_lib_")) {
                log.info("Downloading: %s", href);
                FileUtils.copyURLToFile(new URL(changeFolder + "/" + href), new File(href));
                updateFiles.add(href);
            }
        }

        if (updateFiles.isEmpty())
            throw new BGMessageException("Не найдены файлы обновлений.");

        Scripts.backupInstallRestart(updateFiles);

        return status(conSet, form);
    }

    public ActionForward userLoggedList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setResponseData("logged", LoginStat.getLoginStat().getLoggedUserWithSessions());
        return data(conSet, form, JSP_PATH + "/user_logged_list.jsp");
    }

}