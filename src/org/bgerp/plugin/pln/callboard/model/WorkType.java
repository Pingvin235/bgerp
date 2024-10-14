package org.bgerp.plugin.pln.callboard.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.model.base.IdTitle;
import org.bgerp.plugin.pln.callboard.model.work.ShiftData;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ProcessLinkExpressionObject;
import ru.bgcrm.dao.expression.ProcessParamExpressionObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;

public class WorkType extends IdTitle {
    public static final int MODE_TIME_ON_START = 0;
    public static final int MODE_TIME_ON_STEP = 1;

    private boolean isNonWorkHours;
    private int category;
    private String comment = "";

    private String color;
    private List<String> shortcutList;
    private int timeSetStep;
    private int timeSetMode;

    private String ruleConfig;
    private List<Rule> ruleExpressionList;

    public WorkType() {
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isNonWorkHours() {
        return isNonWorkHours;
    }

    public void setNonWorkHours(boolean isNonWorkHours) {
        this.isNonWorkHours = isNonWorkHours;
    }

    public boolean getIsNonWorkHours() {
        return isNonWorkHours;
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

    public int getTimeSetStep() {
        return timeSetStep;
    }

    public void setTimeSetStep(int step) {
        this.timeSetStep = step;
    }

    public int getTimeSetMode() {
        return timeSetMode;
    }

    public void setTimeSetMode(int distributeMode) {
        this.timeSetMode = distributeMode;
    }

    public String getConfig() {
        StringBuilder result = new StringBuilder();

        Utils.addSetupPair(result, "", "color", String.valueOf(color));
        Utils.addSetupPair(result, "shortcuts", "", Utils.toString(shortcutList));
        Utils.addSetupPair(result, "timeSetStep", "", String.valueOf(timeSetStep));
        Utils.addSetupPair(result, "timeSetMode", "", String.valueOf(timeSetMode));

        return result.toString();
    }

    public void setConfigMap(ConfigMap configMap) {
        color = configMap.get("color", "#000000");
        shortcutList = Utils.toList(configMap.get("shortcuts", ""));
        timeSetStep = configMap.getInt("timeSetStep", configMap.getInt("step", 0));
        timeSetMode = configMap.getInt("timeSetMode", MODE_TIME_ON_START);
    }

    public String getRuleConfig() {
        return ruleConfig;
    }

    public void setRuleConfig(String ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public List<Rule> getRuleExpresionList() {
        if (ruleExpressionList == null) {
            ruleExpressionList = new ArrayList<>();
            for (ConfigMap pm : new Preferences(ruleConfig).subIndexed("rule.").values()) {
                ruleExpressionList.add(new Rule(pm));
            }
        }
        return ruleExpressionList;
    }

    /**
     * Возвращает время в минутах, требуемое для исполнения задачи.
     * @param con
     * @param process
     * @return
     */
    public int getProcessExecuteTime(Connection con, ShiftData shiftData, Process process) {
        int result = -1;

        Map<String, Object> context = new HashMap<>(4);
        context.put(Process.OBJECT_TYPE, process);
        new ProcessParamExpressionObject(con, process.getId()).toContext(context);
        new ProcessLinkExpressionObject(con, process.getId()).toContext(context);
        context.put("shift", shiftData);

        for (Rule rule : getRuleExpresionList()) {
            if (new Expression(context).check(rule.checkExpression)) {
                result = rule.duration;
                break;
            }
        }

        return result;
    }

    public int getSlotCount(int minuteFrom, int minuteTo) {
        if (timeSetStep <= 0) {
            return 1;
        }

        return (minuteTo - minuteFrom) / timeSetStep;
    }

    private static class Rule {
        private String checkExpression;
        private int duration;

        private Rule(ConfigMap config) {
            this.checkExpression = config.get(Expression.CHECK_EXPRESSION_CONFIG_KEY, "");
            this.duration = config.getInt("duration", 0);
        }
    }
}