package org.bgerp.model.file.tmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Temporary uploaded file
 *
 * @author Shamil Vakhitov
 */
public class FileInfo {
    private final String title;
    private final String path;
    private final String hash;

    FileInfo(String title, String path, String hash) {
        this.title = title;
        this.path = path;
        this.hash = hash;
    }

    public String getTitle() {
        return title;
    }

    String getHash() {
        return hash;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(path);
    }

    public void delete() {
        new File(path).delete();
    }
}