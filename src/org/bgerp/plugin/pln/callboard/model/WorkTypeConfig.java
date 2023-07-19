package org.bgerp.plugin.pln.callboard.model;

import java.util.List;

import org.bgerp.app.cfg.Preferences;

import ru.bgcrm.util.Utils;

public class WorkTypeConfig {
    public static final int MODE_TIME_ON_START = 0;
    public static final int MODE_TIME_ON_STEP = 1;

    private String color;
    private List<String> shortcutList;
    private int step;
    private int distributeMode;

    public WorkTypeConfig() {
    }

    public WorkTypeConfig(String config) {
        Preferences configMap = new Preferences(config);

        color = configMap.get("color", "#000000");
        shortcutList = Utils.toList(configMap.get("shortcuts", ""));
        step = configMap.getInt("step", 0);
        distributeMode = configMap.getInt("distributeMode", MODE_TIME_ON_START);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getShortcutList() {
        return shortcutList;
    }

    public void setShortcutList(List<String> shortcutList) {
        this.shortcutList = shortcutList;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getDistributeMode() {
        return distributeMode;
    }

    public void setDistributeMode(int distributeMode) {
        this.distributeMode = distributeMode;
    }

    public String serializeToData() {
        StringBuilder result = new StringBuilder();

        Utils.addSetupPair(result, "", "color", String.valueOf(color));
        Utils.addSetupPair(result, "shortcuts", "", Utils.toString(shortcutList));
        Utils.addSetupPair(result, "step", "", String.valueOf(step));
        Utils.addSetupPair(result, "distributeMode", "", String.valueOf(distributeMode));

        return result.toString();
    }
}