package org.bgerp.app.exec.scheduler;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ru.bgcrm.util.Utils;

/**
 * Scheduler for running single or periodical tasks.
 *
 * @author Shamil Vakhitov
 */
public class Scheduler extends Thread {
    private static final Log log = Log.getLog();

    private static final Scheduler INSTANCE = new Scheduler();
    private static final long SLEEP_TIME_MS = Duration.ofSeconds(60).toMillis();

    public static final Scheduler getInstance() {
        return INSTANCE;
    }

    /** Executor service, created only when the thread has started. */
    private ThreadPoolExecutor pool;

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
                        String state = config.getNotRunnableState();
                        if (state != null) {
                            log.info("Skipping not runnable task: {}", state);
                            continue;
                        }

                        runTask(config);
                    }
                } else {
                    log.warn("Execution pool max size was reached.");
                }

                sleep(SLEEP_TIME_MS);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public void runTask(TaskConfig taskConfig, boolean wait) throws Exception {
        log.info("runTask {}; wait: {}", taskConfig, wait);

        String state = taskConfig.getNotRunnableState();
        if (Utils.notBlankString(state))
            throw new IllegalArgumentException("The task isn't runnable: " + state);

        if (wait) {
            try {
                taskConfig.taskRun();
            } finally {
                taskConfig.taskDone();
            }
        } else {
            runTask(taskConfig);
        }
    }

    private void runTask(TaskConfig config) {
        log.info("Running scheduled task: {}", config);

        Runnable wrapper = new Runnable() {
            @Override
            public void run() {
                try {
                    config.taskRun();
                } catch (Exception e) {
                    log.error(e);
                } finally {
                    config.taskDone();
                }
            }
        };

        pool.execute(wrapper);
    }

    public int getRunningTaskCount() {
        return pool == null ? 0 : pool.getActiveCount();
    }
}