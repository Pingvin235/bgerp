package org.bgerp.app.scheduler;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bgerp.util.Log;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ru.bgcrm.util.Setup;

/**
 * Scheduler for running single or periodical tasks.
 *
 * @author Shamil Vakhitov
 */
public class Scheduler extends Thread {
    private static final Log log = Log.getLog();

    private static final Scheduler INSTANCE = new Scheduler();
    private static final int SLEEP_TIME = 60 * 1000;

    public static final Scheduler getInstance() {
        return INSTANCE;
    }

    /** Executor service, created only when the thread has started. */
    private ThreadPoolExecutor pool;
    /** Key - class name, for controlling of duplicated running tasks. */
    private final Map<String, TaskConfig> runningTasks = new ConcurrentHashMap<>();

    private Scheduler() {
        setName("scheduler");
        start();
    }

    @Override
    public void run() {
        if (!Setup.getSetup().getBoolean("scheduler.start", true)) {
            log.info("Skipping scheduler start.");
            return;
        }

        var namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("scheduler-%d").build();
        pool = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), namedThreadFactory);

        log.info("Starting scheduler..");

        while (true) {
            try {
                if (pool.getActiveCount() < pool.getMaximumPoolSize()) {
                    List<TaskConfig> configs = Setup.getSetup().getConfig(TasksConfig.class).taskConfigsToRun(ZonedDateTime.now());
                    for (TaskConfig config : configs) {
                        String className = config.getClassName();
                        if (runningTasks.get(className) != null) {
                            log.info("Skipping running already started task: {}", config);
                            continue;
                        }

                        log.info("Running scheduled task: {}", config);

                        Runnable wrapper = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    runningTasks.put(className, config);
                                    config.taskRun();
                                } catch (Exception e) {
                                    log.error(e);
                                } finally {
                                    runningTasks.remove(className);
                                    config.taskDone();
                                }
                            }
                        };

                        pool.execute(wrapper);
                    }
                } else {
                    log.warn("Execution pool size was reached.");
                }

                sleep(SLEEP_TIME);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public boolean hasRunningTasks() {
        return pool != null && pool.getActiveCount() > 0;
    }
}