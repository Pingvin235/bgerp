package ru.bgcrm.plugin.report;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import ru.bgcrm.plugin.report.event.listener.PrintQueueEventListener;
import ru.bgcrm.plugin.report.model.Config;
import ru.bgcrm.plugin.report.model.Report;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.XMLUtils;
import ru.bgerp.util.Log;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    private static final Log log = Log.getLog();

    public static final String ID = "report";

    @Deprecated
    private List<Report> reportList = new ArrayList<Report>();
    @Deprecated
    private Map<String, Report> reportMap = new HashMap<String, Report>();

    public Plugin() {
        super(ID);
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        new PrintQueueEventListener();

        final File reportDir = new File("report");
        if (reportDir.exists() && reportDir.canRead() && reportDir.isDirectory())
            reloadReportList(reportDir);
    }

    private void reloadReportList(File reportDir) {
        log.warn("Reloading reports dir. Move report definitions to configuration.");

        reportList.clear();
        for (File file : reportDir.listFiles()) {
            try {
                Document reportDoc = XMLUtils.parseDocument(new InputSource(new FileInputStream(file)));

                Report report = new Report(reportDoc);
                reportList.add(report);
                reportMap.put(report.getId(), report);
            } catch (Exception e) {
                log.error("Error parse report " + file.getName() + " " + e.getMessage(), e);
            }
        }
    }

    public List<Report> getReportList() {
        Config config = Setup.getSetup().getConfig(Config.class);
        return reportList.isEmpty() ? config.getReportList()
                : Streams.concat(config.getReportList().stream(), reportList.stream()).collect(Collectors.toList());
    }

    public Map<String, Report> getReportMap() {
        Config config = Setup.getSetup().getConfig(Config.class);
        return reportMap.isEmpty() ? config.getReportMap()
                : Streams.concat(reportMap.entrySet().stream(), config.getReportMap().entrySet().stream())
                        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

}