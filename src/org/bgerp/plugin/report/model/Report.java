package org.bgerp.plugin.report.model;

import org.bgerp.app.cfg.ConfigMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Old way of reports configuration. */
@Deprecated
public class Report {
    private final String id;
    private final String title;
    private final String daoClass;
    private final String jspFile;

    @Deprecated
    public Report(Document doc) {
        Element docEl = doc.getDocumentElement();

        id = docEl.getAttribute("id");
        daoClass = null;
        jspFile = docEl.getAttribute("jspFile");
        title = docEl.getAttribute("title");
    }

    public Report(String id, ConfigMap config) {
        this.id = id;
        this.daoClass = config.get("daoClass");
        this.jspFile = config.get("jspFile");
        this.title = config.get("title");
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDaoClass() {
        return daoClass;
    }

    public String getJspFile() {
        return jspFile;
    }
}