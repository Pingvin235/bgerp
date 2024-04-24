package org.bgerp.app.exec.scheduler;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.iface.Title;

/**
 * Configurable scheduled task.
 *
 * @author Shamil Vakhitov
 */
public abstract class Task extends Config implements Runnable, Title {
    public Task(ConfigMap config) {
        super(null);
    }

    @Override
    public String getTitle() {
        return "??? " + this.getClass().getName();
    }
}