package ru.bgcrm.plugin.phpbb;

import org.w3c.dom.Document;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "phpbb";

    public Plugin(Document doc) {
        super(doc, ID);
    }
}
