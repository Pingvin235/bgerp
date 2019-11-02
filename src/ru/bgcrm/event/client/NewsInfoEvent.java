package ru.bgcrm.event.client;

import java.util.List;

/**
 * Сообщение с информацией о количестве непрочитанных новостей и сообщений.
 */
public class NewsInfoEvent extends ClientEvent {
	private final int newsCount;
	private final List<Integer> popupNews;

	private final int messagesCount;

	private final boolean blinkNews;
	private final boolean blinkMessages;

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
}