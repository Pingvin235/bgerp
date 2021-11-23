package org.bgerp.event;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.event.UserEvent;
import ru.bgcrm.model.FileData;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Get files for attaching to message.
 *
 * @author Shamil Vakhitov
 */
public class ProcessFilesEvent extends UserEvent {
    private final int processId;
    private final List<FileData> files = new ArrayList<>();

    public ProcessFilesEvent(DynActionForm form, int processId) {
        super(form);
        this.processId = processId;
    }

    public int getProcessId() {
        return processId;
    }

    public void addFile(FileData value) {
        files.add(value);
    }

    public List<FileData> getFiles() {
        return files;
    }
}
