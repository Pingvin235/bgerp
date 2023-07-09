package org.bgerp.app.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

import org.bgerp.app.bean.Bean;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import ru.bgcrm.model.BGException;
import ru.bgcrm.util.ParameterMap;

public class TaskConfig {
    private static final Log log = Log.getLog();

    private final String id;
    private final Class<? extends Runnable> clazz;

    private final String minute;
    private final String hour;
    private final String dayOfMonth;
    private final String month;
    private final String dayOfWeek;
    private final ExecutionTime executionTime;
    private final boolean enabled;

    private Date lastExecutionStart;
    private Duration lastExecutionDuration;

    @SuppressWarnings("unchecked")
    TaskConfig(String id, ParameterMap config) throws BGException {
        this.id = id;

        String className = config.get("class");
        try {
            clazz = (Class<? extends Runnable>) Bean.getClass(className);
        } catch (ClassNotFoundException e) {
            throw new BGException("Task class not found: " + className);
        }

        String expression = new StringBuilder(100)
            .append(minute = config.get("minutes", "*")).append("\t")
            .append(hour = config.get("hours", "*")).append("\t")
            .append(dayOfMonth = config.get("dm", "*")).append("\t")
            .append(month = config.get("month", "*")).append("\t")
            .append(dayOfWeek = config.get("dw", "*"))
            .toString();

        Cron cron = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)).parse(expression);
        executionTime = ExecutionTime.forCron(cron);

        enabled = config.getBoolean("enable", true);

        log.info("Class: {}; schedule: {}; enabled: {}", clazz.getName(), cron.asString(), enabled);
    }

    @Dynamic
    public String getId() {
        return id;
    }

    @Dynamic
    public String getClassName() {
        return clazz.getName();
    }

    @Dynamic
    public String getMinute() {
        return minute;
    }

    @Dynamic
    public String getHour() {
        return hour;
    }

    @Dynamic
    public String getDayOfMonth() {
        return dayOfMonth;
    }

    @Dynamic
    public String getMonth() {
        return month;
    }

    @Dynamic
    public String getDayOfWeek() {
        return dayOfWeek;
    }

    @Dynamic
    public boolean isEnabled() {
        return enabled;
    }

    boolean checkTime(ZonedDateTime time) {
        return executionTime.isMatch(time);
    }

    void taskRun() throws Exception {
        lastExecutionStart = new Date();
        clazz.getDeclaredConstructor().newInstance().run();
    }

    void taskDone() {
        lastExecutionDuration = Duration.between(lastExecutionStart.toInstant(), Instant.now());
    }

    @Dynamic
    public Date getLastExecutionStart() {
        return lastExecutionStart;
    }

    @Dynamic
    public Duration getLastExecutionDuration() {
        return lastExecutionDuration;
    }

    @Override
    public String toString() {
        return getClassName();
    }
}