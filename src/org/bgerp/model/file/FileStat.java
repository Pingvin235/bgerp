package org.bgerp.model.file;

import java.io.File;

/**
 * Files statistic.
 *
 * @author Shamil Vakhitov
 */
public class FileStat {
    private int files;
    private int dirs;
    private long size;

    public FileStat(File dir) {
        stat(dir);
    }

    private void stat(File dir) {
        for (var f : dir.listFiles()) {
            if (f.isDirectory()) {
                dirs++;
                stat(f);
            } else {
                files++;
                size += f.length();
            }
        }
    }

    /**
     * @return files count.
     */
    public int files() {
        return files;
    }

    /**
     * @return dirs count.
     */
    public int dirs() {
        return dirs;
    }

    /**
     * @return files size in bytes.
     */
    public long size() {
        return size;
    }

    @Override
    public String toString() {
        return "FileStat [files=" + files + ", dirs=" + dirs + ", size=" + size + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + files;
        result = prime * result + (int) (size ^ (size >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileStat other = (FileStat) obj;
        if (files != other.files)
            return false;
        if (size != other.size)
            return false;
        return true;
    }
}
