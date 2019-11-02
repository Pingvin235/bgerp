package ru.bgcrm.plugin.report.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.util.ParameterMap;

public class Report {
    private final String id;
    private final String title;
    private final String jspFile;    

    @Deprecated
    public Report(Document doc) {
        Element docEl = doc.getDocumentElement();

        id = docEl.getAttribute("id");
        jspFile = docEl.getAttribute("jspFile");
        title = docEl.getAttribute("title");
    }
    
    public Report(String id, ParameterMap config) {
        this.id = id;
        this.jspFile = config.get("jspFile");
        this.title = config.get("title");
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getJspFile() {
        return jspFile;
    }
}