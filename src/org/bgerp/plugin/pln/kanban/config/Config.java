package org.bgerp.plugin.pln.kanban.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.pln.kanban.Plugin;
import org.bgerp.util.Dynamic;

import ru.bgcrm.util.Utils;

public class Config extends org.bgerp.app.cfg.Config {
    private static final List<String> DEFAULT_PALETTE = List.of(
        "#e0e6ed", "#a4d4ff", "#fff1a4", "#ffbe7e", "#9ad78a", "#fd7d89"
    );

    private final boolean previewEnabled;

    /** Key - status ID, value - hex color string */
    private final Map<Integer, String> statusColors;
    /** Default cyclic palette, index - column position, value - hex color string */
    private final List<String> palette;

    /** Key - queue ID, value - status colors overridden for that queue only */
    private final Map<Integer, Map<Integer, String>> queueStatusColors;
    /** Key - queue ID, value - cyclic palette overridden for that queue only */
    private final Map<Integer, List<String>> queuePalettes;

    protected Config(ConfigMap config) {
        super(null);

        ConfigMap pluginConfig = config.sub(Plugin.ID + ":");

        previewEnabled = pluginConfig.getBoolean("preview.enable", true);

        statusColors = loadStatusColors(pluginConfig);
        palette = loadPalette(pluginConfig, DEFAULT_PALETTE);

        queueStatusColors = new HashMap<>();
        queuePalettes = new HashMap<>();
        for (var qe : pluginConfig.subIndexed("queue.").entrySet()) {
            ConfigMap queueConfig = qe.getValue();

            Map<Integer, String> overriddenStatusColors = loadStatusColors(queueConfig);
            if (!overriddenStatusColors.isEmpty())
                queueStatusColors.put(qe.getKey(), overriddenStatusColors);

            List<String> overriddenPalette = loadPalette(queueConfig, palette);
            if (!overriddenPalette.equals(palette))
                queuePalettes.put(qe.getKey(), overriddenPalette);
        }
    }

    private Map<Integer, String> loadStatusColors(ConfigMap config) {
        var result = new TreeMap<Integer, String>();
        for (var me : config.subIndexed("status.").entrySet()) {
            String color = me.getValue().get("color");
            if (Utils.notBlankString(color))
                result.put(me.getKey(), color);
        }

        return Collections.unmodifiableMap(result);
    }

    private List<String> loadPalette(ConfigMap config, List<String> defaultPalette) {
        var result = new TreeMap<Integer, String>();
        for (int i = 0; i < defaultPalette.size(); i++)
            result.put(i, defaultPalette.get(i));

        for (var me : config.subIndexed("palette.").entrySet()) {
            String color = me.getValue().get("color");
            if (Utils.notBlankString(color))
                result.put(me.getKey(), color);
        }

        return List.copyOf(result.values());
    }

    public boolean isPreviewEnabled() {
        return previewEnabled;
    }

    /**
     * Provides the color for a status column, falling back to the queue's own palette,
     * then to the global status color / palette, if not configured more specifically.
     * @param queueId ID of the queue the board is showing
     * @param statusId process status ID
     * @param columnPos zero-based column position, used for the palette fallback
     * @return hex color string, never {@code null}
     */
    @Dynamic
    public String getColor(int queueId, int statusId, int columnPos) {
        Map<Integer, String> overriddenStatusColors = queueStatusColors.get(queueId);
        if (overriddenStatusColors != null) {
            String color = overriddenStatusColors.get(statusId);
            if (Utils.notBlankString(color))
                return color;
        }

        String color = statusColors.get(statusId);
        if (Utils.notBlankString(color))
            return color;

        List<String> effectivePalette = queuePalettes.getOrDefault(queueId, palette);
        return effectivePalette.get(columnPos % effectivePalette.size());
    }
}
