package ru.bgcrm.plugin.document;

import java.util.Set;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "document";

    public Plugin() {
        super(ID);
    }

    @Override
    public Set<String> getUnusedPaths() {
        return Set.of("docpattern");
    }
}
