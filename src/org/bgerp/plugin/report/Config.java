package org.bgerp.plugin.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.report.model.Report;

/**
 * JSP reports configuration.
 */
public class Config extends org.bgerp.app.cfg.Config {
    private final List<Report> reportList = new ArrayList<>();
    /** Key - report ID. */
    private final Map<String, Report> reportMap = new HashMap<>();

    protected Config(ConfigMap config) {
        super(null);
        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed(Plugin.ID + ":report.").entrySet()) {
            Report report = new Report(me.getKey().toString(), me.getValue());
            reportList.add(report);
            reportMap.put(report.getId(), report);
        }
    }

    public List<Report> getReportList() {
        return reportList;
    }

    public Map<String, Report> getReportMap() {
        return reportMap;
    }
}
