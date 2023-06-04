package org.bgerp.plugin.pln.blow.model;

import java.util.HashSet;
import java.util.Set;

import org.bgerp.model.base.IdTitle;

import ru.bgcrm.model.process.Process;

/**
 * Process executor - user or group.
 *
 * @author Shamil Vakhitov
 */
public class Executor extends IdTitle {
    public Set<Process> processes = new HashSet<>();

    public void add(Process process) {
        processes.add(process);
    }
}