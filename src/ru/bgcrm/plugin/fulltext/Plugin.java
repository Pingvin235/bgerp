package ru.bgcrm.plugin.fulltext;

import org.w3c.dom.Document;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "fulltext";

    public Plugin(Document doc) {
        super(doc, ID);
        new EventListener();
    }
}
