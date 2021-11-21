package org.bgerp.event;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.event.Event;
import ru.bgcrm.event.UserEvent;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.IdStringTitle;
import ru.bgcrm.model.Pair;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Get files for attaching to message.
 *
 * @author Shamil Vakhitov
 */
public class FilesEvent extends UserEvent {
    /** Object type for providing files. */
    private final String objectType;
    /** Object ID for providing files. */
    private final int objectId;
    /** Key - sting, prefixed by a plugin ID. */
    private final List<IdStringTitle> files = new ArrayList<>();

    public FilesEvent(DynActionForm form, String objectType, int objectId) {
        super(form);
        this.objectType = objectType;
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public int getObjectId() {
        return objectId;
    }

    public void addFile(IdStringTitle value) {
        files.add(value);
    }

    public List<IdStringTitle> getFiles() {
        return files;
    }

    /** Retrieve a file data. */
    public static final class Get extends UserEvent {
        /** Object type for providing files. */
        private final String objectType;
        /** Object ID for providing files. */
        private final int objectId;
        /* File ID, has to be prefixed by a plugin ID. */
        private final String id;
        /** Resulting data: name and content of the file. */
        private Pair<String, byte[]> data;

        public Get(DynActionForm form, String objectType, int objectId, String id) {
            super(form);
            this.objectType = objectType;
            this.objectId = objectId;
            this.id = id;
        }

        public String getObjectType() {
            return objectType;
        }

        public int getObjectId() {
            return objectId;
        }

        public String getId() {
            return id;
        }

        public Pair<String, byte[]> getData() {
            return data;
        }

        public void setData(Pair<String, byte[]> data) {
            this.data = data;
        }
    }
}
