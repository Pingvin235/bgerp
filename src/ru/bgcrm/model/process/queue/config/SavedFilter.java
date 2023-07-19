package ru.bgcrm.model.process.queue.config;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdTitle;

/**
 * Фильтр очереди, доступный другим пользователям.
 */
public class SavedFilter extends IdTitle {
    private final int queueId;
    private final String url;

    public SavedFilter(int id, ConfigMap config) {
        this.id = id;
        this.queueId = config.getInt("queueId", 0);
        this.title = config.get("title");
        this.url = config.get("url");
    }

    public SavedFilter(int queueId, int id, String title, String url) {
        super(id, title);
        this.queueId = queueId;
        this.title = title;
        this.url = url;
    }

    public int getQueueId() {
        return queueId;
    }

    public String getUrl() {
        return url;
    }
}
