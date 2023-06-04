package ru.bgcrm.plugin.document.model;

import org.bgerp.model.base.Id;

import ru.bgcrm.model.FileData;

public class Document extends Id {
    private String objectType;
    private int objectId;
    private int fileDataId;
    private FileData fileData;

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public int getFileDataId() {
        return fileDataId;
    }

    public void setFileDataId(int fileDataId) {
        this.fileDataId = fileDataId;
    }

    public FileData getFileData() {
        return fileData;
    }

    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }
}
