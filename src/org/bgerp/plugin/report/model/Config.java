package org.bgerp.plugin.report.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.plugin.report.Plugin;
import ru.bgcrm.util.ParameterMap;

/**
 * Old way of reports configuration.
 */
@Deprecated
public class Config extends ru.bgcrm.util.Config {
    private final List<Report> reportList = new ArrayList<>();
    /** Key - report ID. */
    private final Map<String, Report> reportMap = new HashMap<>();
    
    protected Config(ParameterMap config) {
        super(null);
        for (Map.Entry<Integer, ParameterMap> me : config.subIndexed(Plugin.ID + ":report.").entrySet()) {
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
