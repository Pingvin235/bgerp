package ru.bgcrm.model.process.config;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

public class ProcessPriorityConfig extends Config {
    private static final String PREFIX = "process.priority.";

    private static final SortedMap<Integer, String> DEFAULT_COLORS = Collections.unmodifiableSortedMap(new TreeMap<>(Map.of(
        0, "FFFFFF",
        1, "FFFFFF",
        2, "9AD78A",
        3, "9AD78A",
        4, "FFF1A4",
        5, "FFF1A4",
        6, "FFBE7E",
        7, "FFBE7E",
        8, "FFBE7E",
        9, "FD7D89"
    )));

    /** Key - priority, value - hex color string. */
    private final SortedMap<Integer, String> priorityColors;

    protected ProcessPriorityConfig(ParameterMap config) {
        super(null);
        priorityColors = loadPriorityColors(config);
    }

    private SortedMap<Integer, String> loadPriorityColors(ParameterMap config) {
        var map = config.subIndexed(PREFIX);
        if (map.isEmpty()) {
            return DEFAULT_COLORS;
        }

        var result = new TreeMap<Integer, String>();
        for (var me : map.entrySet())
            result.put(me.getKey(), me.getValue().get("color", "red"));

        return Collections.unmodifiableSortedMap(result);
    }

    /**
     * @return map with key process priority and value hex color string.
     */
    public SortedMap<Integer, String> getPriorityColors() {
        return priorityColors;
    }
}
