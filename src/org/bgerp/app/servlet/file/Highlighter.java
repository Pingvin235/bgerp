package org.bgerp.app.servlet.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * File name highlighter using CSS class
 *
 * @author Shamil Vakhitov
 */
public class Highlighter {
    public static Highlighter LOG_WARN = new Highlighter("*.warn.log") {
        @Override
        protected boolean hasError(String line) {
            // in normal case the log must be empty
            return true;
        }
    };

    public static Highlighter LOG_UPDATE_EXCEPTION = new Highlighter("update_*.log") {
        @Override
        protected boolean hasError(String line) {
            return line.contains("ERROR") || line.contains("REMOVE SOON");
        }
    };

    private final FileFilter fileFilter;

    /**
     * Constructor
     * @param wildcard wildcard for matched files
     */
    private Highlighter(String wildcard) {
        this.fileFilter = WildcardFileFilter.builder().setWildcards(wildcard).get();
    }

    /**
     * Does a file name match to the highlighter
     * @param file the file name
     * @return
     */
    public boolean match(File file) {
        return fileFilter.accept(file);
    }

    /**
     * CSS class to highlight a file
     * @param file the file
     * @return the class name or {@code null}
     * @throws FileNotFoundException
     */
    public String highlight(File file) throws FileNotFoundException {
        try (var scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (hasError(line))
                    return "error";
            }
        }
        return null;
    }

    /**
     * Detect errors in a file line
     * @param line the line
     * @return
     */
    protected boolean hasError(String line) {
        return false;
    }
}
