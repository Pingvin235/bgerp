package org.bgerp.event;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.event.base.UserEvent;
import org.bgerp.model.base.IdStringTitle;
import org.bgerp.model.file.FileData;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Get files for attaching to message.
 *
 * @author Shamil Vakhitov
 */
public class ProcessFilesEvent extends UserEvent {
    private final int processId;
    /** Already DB stored FileData objects. */
    private final List<FileData> files = new ArrayList<>();
    /** Announced files, got after using {@link ProcessFileGetEvent}.*/
    private final List<IdStringTitle> announcedFiles = new ArrayList<>();

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

    public void addAnnouncedFile(IdStringTitle value) {
        announcedFiles.add(value);
    }

    public List<IdStringTitle> getAnnouncedFiles() {
        return announcedFiles;
    }
}
