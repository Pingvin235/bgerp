package ru.bgcrm.dyn;

import org.bgerp.util.Log;

public class ExampleRunnable implements Runnable {
    private static final Log log = Log.getLog();

    @Override
    public void run() {
        log.info("Running test");
    }
}