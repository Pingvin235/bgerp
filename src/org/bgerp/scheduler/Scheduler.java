package org.bgerp.scheduler;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bgerp.util.Log;

import ru.bgcrm.util.Setup;

/**
 * Scheduler for running single or periodical tasks.
 *
 * @author Shamil Vakhitov
 */
public class Scheduler extends Thread {
    static final Log log = Log.getLog();

    private static final int SLEEP_TIME = 60 * 1000;

    private static final Scheduler INSTANCE = new Scheduler();

    public static final Scheduler getInstance() {
        return INSTANCE;
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
                    List<Runnable> taskList = config.getTasksToRun(new GregorianCalendar());
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
                log.error(e);
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