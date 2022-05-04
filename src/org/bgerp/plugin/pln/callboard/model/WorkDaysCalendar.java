package org.bgerp.plugin.pln.callboard.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bgerp.plugin.pln.callboard.model.config.DayTypeConfig;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Pair;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;

public class WorkDaysCalendar extends IdTitle {
    private String comment = "";
    private List<WorkDaysCalendarRule> rules = new ArrayList<WorkDaysCalendarRule>();

    public WorkDaysCalendar() {
    }

    public WorkDaysCalendar(int id, String title, String comment, List<WorkDaysCalendarRule> rules) {
        super(id, title);

        this.comment = comment;
        this.rules = rules;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Pair<DayType, Boolean> getDayType(Date date, Map<Date, Integer> excludeDates) {
        DayTypeConfig config = Setup.getSetup().getConfig(DayTypeConfig.class);

        Integer result = null;

        if (excludeDates != null) {
            result = excludeDates.get(date);
            if (result != null) {
                return new Pair<DayType, Boolean>(config.getType(result), true);
            }
        }

        int dayOfWeek = TimeUtils.getDayOfWeekPosition(date);
        for (WorkDaysCalendarRule rule : rules) {
            if (rule.getDay() == dayOfWeek) {
                result = rule.getType();
                break;
            }
        }

        if (result != null) {
            return new Pair<DayType, Boolean>(config.getType(result), false);
        }

        return null;
    }
}