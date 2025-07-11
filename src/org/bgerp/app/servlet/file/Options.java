package org.bgerp.app.servlet.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Files fluent options.
 *
 * @author Shamil Vakhitov
 */
public class Options {
    private Order order;
    private boolean deletionEnabled;
    private boolean downloadEnabled;
    private final List<Highlighter> highlighters = new ArrayList<>();

    public Options withOrder(Order value) {
        order = value;
        return this;
    }

    public Options withDownloadEnabled() {
        downloadEnabled = true;
        return this;
    }

    public Options withDeletionEnabled() {
        deletionEnabled = true;
        return this;
    }

    public Options withHighlighter(Highlighter... highlighter) {
        highlighters.addAll(List.of(highlighter));
        return this;
    }

    public Order getOrder() {
        return order;
    }

    public boolean isDownloadEnabled() {
        return downloadEnabled;
    }

    public boolean isDeletionEnabled() {
        return deletionEnabled;
    }

    /**
     * Finds highlighter for a file
     * @param file the file
     * @return
     */
    public Highlighter highlighter(File file) {
        return highlighters.stream().filter(hl -> hl.match(file)).findFirst().orElse(null);
    }
}
