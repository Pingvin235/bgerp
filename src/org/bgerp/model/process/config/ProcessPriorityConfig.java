package org.bgerp.model.process.config;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.util.Dynamic;

import ru.bgcrm.util.Utils;

@Bean
public class ProcessPriorityConfig extends Config {
    private static final String PREFIX = "process.priority.";

    private static final SortedMap<Integer, String> DEFAULT_COLORS = Collections.unmodifiableSortedMap(new TreeMap<>(Map.of(
        0, "#ffffff",
        1, "#ffffff",
        2, "#9ad78a",
        3, "#9ad78a",
        4, "#fff1a4",
        5, "#fff1a4",
        6, "#ffbe7e",
        7, "#ffbe7e",
        8, "#ffbe7e",
        9, "#fd7d89"
    )));

    /** Key - priority, value - hex color string */
    private final SortedMap<Integer, String> priorityColors;
    /** Key - priority, value - description string */
    private final Map<Integer, String> priorityDescriptions;

    protected ProcessPriorityConfig(ConfigMap config) {
        super(null);
        priorityColors = loadPriorityColors(config);
        priorityDescriptions = loadPriorityDescriptions(config);
    }

    private SortedMap<Integer, String> loadPriorityColors(ConfigMap config) {
        var map = config.subIndexed(PREFIX);

        var result = new TreeMap<Integer, String>();
        for (var me : map.entrySet()) {
            String color = me.getValue().get("color");
            if (Utils.notBlankString(color))
                result.put(me.getKey(), color);
        }

        if (result.isEmpty())
            return DEFAULT_COLORS;

        return Collections.unmodifiableSortedMap(result);
    }

    private Map<Integer, String> loadPriorityDescriptions(ConfigMap config) {
        var map = config.subIndexed(PREFIX);

        var result = new TreeMap<Integer, String>();
        for (var me : map.entrySet())
            result.put(me.getKey(), me.getValue().get("description", ""));

        return Collections.unmodifiableMap(result);
    }

    /**
     * @return map with key process priority and value hex color string.
     */
    @Dynamic
    public SortedMap<Integer, String> getPriorityColors() {
        return priorityColors;
    }

    /**
     * Provides a description for a given priority
     * @param value the priority
     * @return the description, can be {@code null}
     */
    public String getPriorityDescription(int value) {
        return priorityDescriptions.get(value);
    }
}
