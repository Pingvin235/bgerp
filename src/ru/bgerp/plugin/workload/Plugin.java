package ru.bgerp.plugin.workload;

import org.w3c.dom.Document;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "workload";

    public Plugin(Document doc) {
        super(doc, ID);
    }
}
