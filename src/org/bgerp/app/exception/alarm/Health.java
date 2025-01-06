package org.bgerp.app.exception.alarm;

import ru.bgcrm.util.AdminPortListener;

class Health {
    private static final long SYSTEM_CHECK_INTERVAL = 2 * 1000;
    private static final float MEMORY_CHECK_THRESHOLD = 0.85f;

    private long lastSystemCheck = 0;

    void check() {
        long now = System.currentTimeMillis();
        if (now - lastSystemCheck > SYSTEM_CHECK_INTERVAL) {
            Runtime r = Runtime.getRuntime();
            if ((r.maxMemory() * MEMORY_CHECK_THRESHOLD) < (r.totalMemory() - r.freeMemory())) {
                AlarmSender.send("app.low.memory", 30 * 1000, "Low free memory", () -> "Application has too less of free memory.\n" + AdminPortListener.memoryStatus());

                r.gc();
            }
            lastSystemCheck = now;
        }
    }
}
