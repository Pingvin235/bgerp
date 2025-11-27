package org.bgerp.app.exec.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.Bean;
import org.bgerp.model.base.iface.IdTitle;
import org.bgerp.model.base.iface.Title;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

/**
 * Scheduled task configuration
 *
 * @author Shamil Vakhitov
 */
public class TaskConfig implements IdTitle<String> {
    private static final Log log = Log.getLog();

    private static final Set<Class<?>> RUNNING_CLASSES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final String id;
    private final ConfigMap config;

    private final Class<? extends Task> clazz;

    private final String minute;
    private final String hour;
    private final String dayOfMonth;
    private final String month;
    private final String dayOfWeek;
    private final ExecutionTime executionTime;
    private final boolean enabled;

    private final AtomicBoolean running = new AtomicBoolean();
    private Date lastRunStart;
    private Duration lastRunDuration;

    @SuppressWarnings("unchecked")
    TaskConfig(String id, ConfigMap config) throws Exception {
        this.id = id;
        this.config = config;

        this.clazz = (Class<? extends Task>) Bean.getClass(config.get("class"));

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
    @Override
    public String getId() {
        return id;
    }

    @Dynamic
    @Override
    public String getTitle() {
        try {
            Runnable task = taskInstance();
            if (task instanceof Title titled)
                return titled.getTitle();
            return task.getClass().getSimpleName();
        } catch (Exception e) {
            return e.getMessage();
        }
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

    /**
     * @return {@code null} if the task can be run, or the state, preventing that
     */
    @Dynamic
    public String getNotRunnableState() {
        if (running.get())
            return "Running task";

        if (RUNNING_CLASSES.contains(clazz))
            return "Running class";

        return null;
    }

    @Dynamic
    public Date getLastRunStart() {
        return lastRunStart;
    }

    @Dynamic
    public Duration getLastRunDuration() {
        return lastRunDuration;
    }

    @Override
    public String toString() {
        return "Task " + clazz.getName();
    }

    void taskRun() throws Exception {
        RUNNING_CLASSES.add(clazz);
        running.set(true);
        lastRunStart = new Date();
        taskInstance().run();
    }

    void taskDone() {
        RUNNING_CLASSES.remove(clazz);
        running.set(false);
        lastRunDuration = Duration.between(lastRunStart.toInstant(), Instant.now());
    }

    private Runnable taskInstance() throws Exception {
        try {
            return clazz.getDeclaredConstructor(ConfigMap.class).newInstance(config);
        } catch (NoSuchMethodException e) {
            // no constructor with ParameterMap was found
        } catch (Exception e) {
            throw e;
        }

        return clazz.getDeclaredConstructor().newInstance();
    }
}

