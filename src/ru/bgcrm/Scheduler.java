package ru.bgcrm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Планировщик - запускает определённые в конфигурации периодические задачи и
 * разовые задачи, переданные приложением.
 */
public class Scheduler extends Thread {
    private static class TaskConfig {
        private final Class<? extends Runnable> clazz;
        private final Set<Integer> daysOfWeek;
        private final Set<Integer> hours;
        private final Set<Integer> minutes;
        private final boolean enable;

        private final ParameterMap config;

        @SuppressWarnings("unchecked")
        public TaskConfig(ParameterMap config) throws BGException {
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
            enable = config.getBoolean("enable", true);

            log.info("Class: " + clazz.getName() + "; dw: " + daysOfWeek + "; hours: " + hours + "; minutes: " + minutes + "; enable: " + enable);
        }

        public boolean checkTime(Calendar time) {
            return (daysOfWeek.size() == 0 || daysOfWeek.contains(TimeUtils.getDayOfWeekPosition(time)))
                    && (hours.size() == 0 || hours.contains(time.get(Calendar.HOUR_OF_DAY)))
                    && (minutes.size() == 0 || minutes.contains(time.get(Calendar.MINUTE)));
        }

        public Runnable taskInstance() throws Exception {
            if (ConfigurableTask.class.isAssignableFrom(clazz))
                return clazz.getConstructor(ParameterMap.class).newInstance(config);
            return clazz.getDeclaredConstructor().newInstance();
        }
    }

    private static class TasksConfig extends Config {
        private List<TaskConfig> tasks = new ArrayList<TaskConfig>();

        public TasksConfig(ParameterMap setup) {
            super(setup);

            log.info("Reload tasks config.");

            final String prefix = "scheduler.task.";
            for (Map.Entry<Integer, ParameterMap> me : setup.subIndexed(prefix).entrySet()) {
                Integer taskId = me.getKey();
                try {
                    tasks.add(new TaskConfig(setup.sub(prefix + taskId + ".")));
                } catch (Exception e) {
                    log.error("Load task config " + taskId + " error: " + e.getMessage(), e);
                }
            }
        }

        public List<Runnable> getTaskForRun(Calendar time) {
            List<Runnable> result = new ArrayList<Runnable>();

            for (TaskConfig config : tasks) {
                if (config.enable && config.checkTime(time)) {
                    try {
                        result.add(config.taskInstance());
                    } catch (Exception e) {
                        log.error("Error create task instance: " + e.getMessage(), e);
                    }
                }
            }

            return result;
        }
    }

    public abstract static class ConfigurableTask implements Runnable {
        protected final ParameterMap config;

        public ConfigurableTask(ParameterMap config) {
            this.config = config;
        }
    }

    private static final Logger log = Logger.getLogger(Scheduler.class);
    private static final int SLEEP_TIME = 60 * 1000;

    private static Scheduler instance = new Scheduler();

    public static final Scheduler getInstance() {
        return instance;
    }

    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    private final ConcurrentHashMap<Class<?>, Date> runningTasks = new ConcurrentHashMap<Class<?>, Date>();

    private Scheduler() {
        start();
    }

    @Override
    public void run() {
        if (!Setup.getSetup().getBoolean("scheduler.start", true) || "0".equals(System.getProperty("scheduler.start"))) {
            log.info("Skipping scheduler start.");
            return;
        }

        log.info("Starting scheduler..");

        while (true) {
            try {
                TasksConfig config = Setup.getSetup().getConfig(TasksConfig.class);

                if (pool.getActiveCount() != pool.getMaximumPoolSize()) {
                    List<Runnable> taskList = config.getTaskForRun(new GregorianCalendar());
                    for (Runnable task : taskList) {
                        final Runnable runningTask = task;
                        final Class<?> runningClass = task.getClass();

                        if (runningTasks.get(runningClass) != null) {
                            log.info("Skipping running already started task: " + task);
                            continue;
                        }

                        log.info("Running scheduled task: " + task);

                        Runnable wrapper = new Runnable() {
                            @Override
                            public void run() {
                                runningTasks.put(runningClass, new Date());
                                runningTask.run();
                                runningTasks.remove(runningClass);
                            }
                        };

                        pool.execute(wrapper);
                    }
                } else {
                    //TODO: Аларм, что запуск задачи не произведёт из-за незавершившихся предыдущих задач.
                }

                sleep(SLEEP_TIME);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public int getActiveTaskCount() {
        return pool.getActiveCount();
    }

    public ConcurrentHashMap<Class<?>, Date> getRunningTasks() {
        return runningTasks;
    }

    /**
     * Запуск разовой задачи.
     * @param task
     */
    public void startTask(Runnable task) {
        log.info("Running one time task: " + task);
        pool.execute(task);
    }

    public static final void logExecutingTime(Runnable task, long timeStart) {
        log.info("Task " + task + " finished. Executing time: " + (System.currentTimeMillis() - timeStart) + " ms.");
    }
}