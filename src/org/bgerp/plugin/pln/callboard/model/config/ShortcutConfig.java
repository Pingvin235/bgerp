package org.bgerp.plugin.pln.callboard.model.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.pln.callboard.model.Shortcut;

public class ShortcutConfig extends Config {
    private final Map<Integer, Shortcut> shortcutMap = new LinkedHashMap<>();

    public ShortcutConfig(ConfigMap setup) {
        super(null);

        for (Map.Entry<Integer, ConfigMap> me : setup.subIndexed("callboard.workdays.shortcut.").entrySet()) {
            int id = me.getKey();
            ConfigMap config = me.getValue();

            shortcutMap.put(id, new Shortcut(id, config.get("title", ""), config.get("value", "")));
        }
    }

    public Map<Integer, Shortcut> getShortcutMap() {
        return shortcutMap;
    }

    public Collection<Shortcut> getShortcuts() {
        return shortcutMap.values();
    }

    public Shortcut getShortcut(int id) {
        return shortcutMap.get(id);
    }
}
