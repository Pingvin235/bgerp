package ru.bgcrm.plugin.document;

import java.util.List;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "document";

    public Plugin() {
        super(ID);
    }

    @Override
    public Iterable<String> getUnusedPaths() {
        return List.of("docpattern");
    }
}
