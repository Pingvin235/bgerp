package ru.bgcrm.event.process;

import java.util.Set;

import org.bgerp.cache.ProcessTypeCache;

import ru.bgcrm.event.UserEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.struts.form.DynActionForm;

public class ProcessChangingEvent extends UserEvent {
    public static final int MODE_STATUS_CHANGING = 1;
    public static final int MODE_DESCRIPTION_CHANGING = 2;
    public static final int MODE_EXECUTORS_CHANGING = 3;
    public static final int MODE_PRIORITY_CHANGING = 4;
    public static final int MODE_GROUPS_CHANGING = 5;
    public static final int MODE_DESCRIPTION_ADDING = 6;
    public static final int MODE_TYPE_CHANGING = 7;

    private final Process process;
    private final int changeMode;
    private final Object value;

    public ProcessChangingEvent(DynActionForm form, Process process, Object value, int changeMode) {
        super(form);

        this.process = process;
        this.value = value;
        this.changeMode = changeMode;
    }

    public Process getProcess() {
        return process;
    }

    public boolean isStatus() {
        return changeMode == MODE_STATUS_CHANGING;
    }

    public boolean isClosing() {
        if (!isStatus()) {
            return false;
        }

        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        return process.getCloseTime() == null
                && type.getProperties().getCloseStatusIds().contains(getStatusChange().getStatusId());
    }

    public boolean isOpening() {
        if (!isStatus()) {
            return false;
        }

        ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
        return process.getCloseTime() != null
                && !type.getProperties().getCloseStatusIds().contains(getStatusChange().getStatusId());
    }

    public StatusChange getStatusChange() {
        if (!isStatus()) {
            return null;
        }

        return (StatusChange) value;
    }

    public boolean isDescription() {
        return changeMode == MODE_DESCRIPTION_CHANGING;
    }

    public boolean isDescriptionAdd() {
        return changeMode == MODE_DESCRIPTION_ADDING;
    }

    public String getDescription() {
        if (!isDescription() && !isDescriptionAdd()) {
            return null;
        }

        return (String) value;
    }

    public boolean isGroups() {
        return changeMode == MODE_GROUPS_CHANGING;
    }

    public Set<Integer> getGroups() {
        Set<ProcessGroup> processGroups = getProcessGroups();
        if (processGroups == null) {
            return null;
        }

        return ProcessGroup.toGroupSet(processGroups);
    }

    @SuppressWarnings("unchecked")
    public Set<ProcessGroup> getProcessGroups() {
        if (!isGroups()) {
            return null;
        }

        return (Set<ProcessGroup>) value;
    }

    public boolean isExecutors() {
        return changeMode == MODE_EXECUTORS_CHANGING;
    }

    public Set<Integer> getExecutors() {
        Set<ProcessExecutor> processExecutors = getProcessExecutors();
        if (processExecutors == null) {
            return null;
        }

        return ProcessExecutor.toExecutorSet(processExecutors);
    }

    @SuppressWarnings("unchecked")
    public Set<ProcessExecutor> getProcessExecutors() {
        if (!isExecutors()) {
            return null;
        }

        return (Set<ProcessExecutor>) value;
    }

    public boolean isPriority() {
        return changeMode == MODE_PRIORITY_CHANGING;
    }

    public Integer getPriority() {
        if (!isPriority()) {
            return null;
        }

        return (Integer) value;
    }

    public boolean isTypeId() {
        return changeMode == MODE_TYPE_CHANGING;
    }

    public Integer getTypeId() {
        if (!isTypeId()) {
            return null;
        }

        return (Integer) value;
    }
}