package org.bgerp.app.scheduler;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

/**
 * Configurable scheduler task.
 *
 * @author Shamil Vakhitov
 */
public abstract class Task extends Config implements Runnable {
    public Task(ParameterMap config) {
        super(config);
    }
}