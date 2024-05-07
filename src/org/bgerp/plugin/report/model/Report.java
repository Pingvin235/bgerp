package org.bgerp.plugin.report.model;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.servlet.jsp.GetJsp;

/**
 * JSP report configuration.
 */
public class Report implements GetJsp {
    private final String id;
    private final String title;
    private final String jsp;

    public Report(String id, ConfigMap config) {
        this.id = id;
        this.jsp = config.get("jspFile");
        this.title = config.get("title");
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getJsp() {
        return jsp;
    }
}