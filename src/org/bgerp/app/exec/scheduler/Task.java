package org.bgerp.app.exec.scheduler;

import org.bgerp.model.base.iface.Title;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

/**
 * Configurable scheduler task.
 *
 * @author Shamil Vakhitov
 */
public abstract class Task extends Config implements Runnable, Title {
    public Task(ParameterMap config) {
        super(null);
    }
}