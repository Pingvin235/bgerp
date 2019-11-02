package ru.bgcrm.event.client;

/**
 * Сообщение о необходимости перейти в текущую выбранную очередь процессов
 * (если открыты очереди процессов) и обновить её.
 */
public class ProcessCurrentQueueRefreshEvent
	extends ClientEvent
{}