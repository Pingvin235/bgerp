package ru.bgcrm.event.process;

import ru.bgcrm.event.UserEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

public class ProcessChangedEvent extends UserEvent {
    public static final int MODE_CREATED = 1;
    public static final int MODE_STATUS_CHANGED = 2;
    public static final int MODE_DESCRIPTION_CHANGED = 3;
    public static final int MODE_EXECUTORS_CHANGED = 4;
    public static final int MODE_PRIORITY_CHANGED = 5;
    public static final int MODE_CREATED_LINKED = 6;
    public static final int MODE_GROUPS_CHANGED = 7;
    public static final int MODE_DESCRIPTION_ADDED = 8;
    public static final int MODE_TYPE_CHANGED = 9;
    public static final int MODE_CREATE_FINISHED = 10;

    private final Process process;
    private final int changeMode;

    public ProcessChangedEvent(DynActionForm form, Process process, int changeMode) {
        super(form);

        this.process = process;
        this.changeMode = changeMode;
    }

    public Process getProcess() {
        return process;
    }

    public boolean isCreated() {
        return changeMode == MODE_CREATED;
    }

    public boolean isCreatedLinked() {
        return changeMode == MODE_CREATED_LINKED;
    }

    public boolean isStatus() {
        return changeMode == MODE_STATUS_CHANGED;
    }

    public boolean isDescription() {
        return changeMode == MODE_DESCRIPTION_CHANGED;
    }

    public boolean isDescriptionAdd() {
        return changeMode == MODE_DESCRIPTION_ADDED;
    }

    public boolean isGroups() {
        return changeMode == MODE_GROUPS_CHANGED;
    }

    public boolean isExecutors() {
        return changeMode == MODE_EXECUTORS_CHANGED;
    }

    public boolean isPriority() {
        return changeMode == MODE_PRIORITY_CHANGED;
    }

    public boolean isType() {
        return changeMode == MODE_TYPE_CHANGED;
    }

    public boolean isCreateFinished() {
        return changeMode == MODE_CREATE_FINISHED;
    }
}