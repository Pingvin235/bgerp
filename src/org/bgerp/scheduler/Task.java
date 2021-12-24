package org.bgerp.scheduler;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;

/**
 * Scheduler task with extended functionality comparing to simple {@link Runnable}.
 *
 * @author Shamil Vakhitov
 */
public abstract class Task extends Config implements Runnable/* , ru.bgerp.l10n.Titled */ {
    public Task(ParameterMap config) {
        super(config);
    }
}