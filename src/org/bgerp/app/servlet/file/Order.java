package org.bgerp.app.servlet.file;

import java.io.File;
import java.util.Comparator;

/**
 * Sorting comparators.
 *
 * @author Shamil Vakhitov
 */
public enum Order implements Comparator<File> {
    /**
     * Sort by modification time, last modified first.
     */
    LAST_MODIFIED_DESC {
        @Override
        public int compare(File o1, File o2) {
            return (int) ((o2.lastModified() - o1.lastModified()) / 1000);
        }
    },

    /**
     * Sort as in normal file system. First directories, than files.
     */
    NORMAL_FS {
        @Override
        public int compare(File o1, File o2) {
            if (o1.isDirectory())
                return o2.isDirectory() ? o1.compareTo(o2) : -1;
            else if (o2.isDirectory())
                return 1;

            return o1.compareTo(o2);
        }
    }
}
