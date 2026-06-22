package org.bgerp.app.servlet.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Files fluent options.
 *
 * @author Shamil Vakhitov
 */
public class Options {
    private Order order;
    private boolean downloadEnabled;
    private boolean deletionEnabled;
    private WildcardFileFilter deletionByClear;
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

    /**
     * File wildcards for those deletion should be replaced by clean operation
     * @param wildcards the wildcards
     * @return
     */
    public Options withDeletionByClear(String... wildcards) {
        deletionByClear = WildcardFileFilter.builder().setWildcards(wildcards).get();
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
     * Should deletion for a file be replaced by clean operation
     * @param file the file
     * @return
     */
    public boolean isDeletionByClear(File file) {
        return deletionByClear != null && deletionByClear.accept(file);
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
