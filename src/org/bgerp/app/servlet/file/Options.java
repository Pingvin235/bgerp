package org.bgerp.app.servlet.file;

/**
 * Files fluent options.
 *
 * @author Shamil Vakhitov
 */
public class Options {
    private Order order;
    private boolean deletionEnabled;
    private boolean downloadEnabled;

    public Options withOrder(Order value) {
        this.order = value;
        return this;
    }

    public Options withDownloadEnabled() {
        this.downloadEnabled = true;
        return this;
    }

    public Options withDeletionEnabled() {
        this.deletionEnabled = true;
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
}
