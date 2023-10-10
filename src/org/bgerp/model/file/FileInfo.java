package org.bgerp.model.file;

import java.io.FileInputStream;

public class FileInfo {
    public String title;
    public FileInputStream inputStream;

    public FileInfo(String title, FileInputStream inputStream) {
        this.title = title;
        this.inputStream = inputStream;
    }
}