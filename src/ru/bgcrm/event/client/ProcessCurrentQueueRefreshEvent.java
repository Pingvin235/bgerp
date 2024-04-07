package ru.bgcrm.event.client;

import org.bgerp.event.base.ClientEvent;

/**
 * Сообщение о необходимости перейти в текущую выбранную очередь процессов
 * (если открыты очереди процессов) и обновить её.
 */
public class ProcessCurrentQueueRefreshEvent
	extends ClientEvent
{}