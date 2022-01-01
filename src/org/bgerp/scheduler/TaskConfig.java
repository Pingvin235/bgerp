package org.bgerp.scheduler;

import java.util.Calendar;
import java.util.Set;

import org.bgerp.plugin.kernel.Plugin;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class TaskConfig {
    private final Class<? extends Runnable> clazz;
    private final Set<Integer> daysOfWeek;
    private final Set<Integer> hours;
    private final Set<Integer> minutes;
    private final boolean enabled;
    private final boolean manualRun;

    private final ParameterMap config;

    @SuppressWarnings("unchecked")
    TaskConfig(ParameterMap config) throws BGException {
        this.config = config;

        String className = config.get("class");
        try {
            clazz = (Class<? extends Runnable>) DynamicClassManager.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new BGException("Task class not found: " + className);
        }

        daysOfWeek = Utils.toIntegerSet(config.get("dw"));
        hours = Utils.toIntegerSet(config.get("hours"));
        minutes = Utils.toIntegerSet(config.get("minutes"));
        enabled = config.getBoolean("enable", true);
        manualRun = config.getBoolean("manual.run", false);

        Scheduler.log.info("Class: {}; dw: {}; hours: {}; minutes: {}; enabled: {}",
            clazz.getName(), daysOfWeek, hours, minutes, enabled);
    }

    public String getPluginId() {
        return Plugin.ID;
    }

    /* public String getTaskTitle(Localizer l) throws Exception {
        var instance = taskInstance();
        if (!instance.getClass().isAssignableFrom(Task.class)) {
            return "";
        }
        var task = (Task) instance;
        return task.getTitle(l);
    } */

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isManualRun() {
        return manualRun;
    }

    public String getSchedule() {
        var result = new StringBuilder(200);

        result.append("DW: ").append(daysOfWeek.isEmpty() ? "*" : daysOfWeek);

        return result.toString();
    }

    public Runnable taskInstance() throws Exception {
        /* if (Task.class.isAssignableFrom(clazz))
            return clazz.getConstructor(ParameterMap.class).newInstance(config); */
        return clazz.getDeclaredConstructor().newInstance();
    }

    boolean checkTime(Calendar time) {
        return (daysOfWeek.isEmpty() || daysOfWeek.contains(TimeUtils.getDayOfWeekPosition(time)))
                && (hours.isEmpty() || hours.contains(time.get(Calendar.HOUR_OF_DAY)))
                && (minutes.isEmpty() || minutes.contains(time.get(Calendar.MINUTE)));
    }

}