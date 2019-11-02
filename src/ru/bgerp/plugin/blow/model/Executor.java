package ru.bgerp.plugin.blow.model;

import java.util.HashSet;
import java.util.Set;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.process.Process;

/**
 * Исполнитель процесса - пользователь, либо группа.
 * 
 * @author Shamil
 */
public class Executor extends IdTitle {
    public Set<Process> processes = new HashSet<>();
    
    public void add(Process process) {
        processes.add(process);
    }
}