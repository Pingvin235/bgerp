package org.bgerp.event.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.l10n.Localizer;

import ru.bgcrm.event.client.ClientEvent;

/**
 * Unread messages and news state.
 *
 * @author Shamil Vakhitov
 */
public class NewsInfoEvent extends ClientEvent {
    private final int newsCount;
    private final List<Integer> popupNews;

    private final int messagesCount;

    private final boolean blinkNews;
    private final boolean blinkMessages;

    private boolean versionUpdateNeeded;
    private boolean versionLink;

    private final Map<String, String> message = new HashMap<>();

    public NewsInfoEvent(int newsCount, int messageCount, List<Integer> popupNews, boolean blinkNews,
            boolean blinkMessages) {
        this.newsCount = newsCount;
        this.messagesCount = messageCount;
        this.popupNews = popupNews;
        this.blinkNews = blinkNews;
        this.blinkMessages = blinkMessages;
    }

    public int getNewsCount() {
        return newsCount;
    }

    public int getMessagesCount() {
        return messagesCount;
    }

    public List<Integer> getPopupNews() {
        return popupNews;
    }

    public boolean isBlinkNews() {
        return blinkNews;
    }

    public boolean isBlinkMessages() {
        return blinkMessages;
    }

    public void version(boolean versionLink) {
        this.versionUpdateNeeded = true;
        this.versionLink = versionLink;
    }

    public boolean isVersionUpdateNeeded() {
        return versionUpdateNeeded;
    }

    public boolean isVersionLink() {
        return versionLink;
    }

    public void message(Localizer l, String key) {
        message.put(key, l.l(key));
    }

    /**
     * @return map with localized messages.
     */
    public Map<String, String> getMessage() {
        return Collections.unmodifiableMap(message);
    }
}