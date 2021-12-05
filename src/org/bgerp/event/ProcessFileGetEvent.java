package org.bgerp.event;

import ru.bgcrm.event.UserEvent;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Retrieving a file, announced in {@link ProcessFilesEvent#getAnnouncedFiles()}.
 *
 * @author Shamil Vakhitov
 */
public class ProcessFileGetEvent extends UserEvent {
    private final int processId;
    private final String fileId;
    private String fileTitle;
    private byte[] fileData;

    public ProcessFileGetEvent(DynActionForm form, int processId, String fileId) {
        super(form);
        this.processId = processId;
        this.fileId = fileId;
    }

    public int getProcessId() {
        return processId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFile(String title, byte[] data) {
        fileTitle = title;
        fileData = data;
    }

    public String getFileTitle() {
        return fileTitle;
    }

    public byte[] getFileData() {
        return fileData;
    }
}
