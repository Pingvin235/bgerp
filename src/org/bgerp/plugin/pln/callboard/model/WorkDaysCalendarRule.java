package org.bgerp.plugin.pln.callboard.model;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.util.Utils;

public class WorkDaysCalendarRule {
    private int day;
    private int type;

    public WorkDaysCalendarRule(int day, int type) {
        this.day = day;
        this.type = type;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static List<WorkDaysCalendarRule> createFromString(String ruleString) {
        List<WorkDaysCalendarRule> result = new ArrayList<WorkDaysCalendarRule>();

        try {
            for (String rule : Utils.toSet(ruleString, ";")) {
                String headPart = rule.substring(0, rule.indexOf(":"));
                int type = Integer.parseInt(rule.substring(rule.indexOf(":") + 1));

                if (headPart.indexOf(",") > -1) {
                    for (Integer part : Utils.toIntegerSet(headPart)) {
                        result.add(new WorkDaysCalendarRule(part, type));
                    }
                } else if (headPart.indexOf("-") > -1) {
                    int begin = Integer.parseInt(headPart.substring(0, headPart.indexOf("-")));
                    int end = Integer.parseInt(headPart.substring(headPart.indexOf("-") + 1));

                    while (begin <= end) {
                        result.add(new WorkDaysCalendarRule(begin, type));
                        begin++;
                    }
                } else {
                    result.add(new WorkDaysCalendarRule(Integer.parseInt(headPart), type));
                }
            }
        } catch (Exception e) {
            return result;
        }

        return result;
    }
}
