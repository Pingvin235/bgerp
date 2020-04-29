package ru.bgcrm.struts.action.admin;

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
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgerp.util.RuntimeRunner;

import static ru.bgcrm.util.distr.InstallProcessor.UPDATE_TO_CHANGE_URL;

public class AppAction extends BaseAction {

    public ActionForward status(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        form.getResponse().setData("status", AdminPortListener.getStatus());

        FileFilter fileFilter = new WildcardFileFilter("log_update*");
        List<File> logFiles = Lists.newArrayList(new File(".").listFiles(fileFilter)) ;
        logFiles.sort((f1, f2) -> (int) (f2.lastModified() - f1.lastModified()));

        form.getResponse().setData("logUpdateList", logFiles);

        return data(conSet, mapping, form);
    }

    public ActionForward update(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        boolean force = form.getParamBoolean("force");

        new RuntimeRunner(new String[] { 
            "bash", "-c", "./backup.sh && ./installer.sh " + (force ? "updatef" : "update") + " && ./erp_restart.sh" })
            .run();

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
            throw new BGMessageException("Не найдены файлы обновлений");
        
        StringBuilder installerCommand = new StringBuilder(100);
        for (String file : updateFiles) {
            installerCommand.append("&& ./installer.sh install ");
            installerCommand.append(file);
        }
    
        new RuntimeRunner(new String[] { 
            "bash", "-c", "./backup.sh " + installerCommand.toString() + " && ./erp_restart.sh" })
            .run();

        return status(conSet, form);
    }

    public ActionForward userLoggedList(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        form.getResponse().setData("logged", LoginStat.getLoginStat().getLoggedUserWithSessions());

        return data(conSet, mapping, form);
    }

}