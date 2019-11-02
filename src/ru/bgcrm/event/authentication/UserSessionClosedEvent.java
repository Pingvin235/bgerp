package ru.bgcrm.event.authentication;

import javax.servlet.http.HttpSessionEvent;

import ru.bgcrm.event.Event;
import ru.bgcrm.model.user.User;

/**
 * Событие генерируется при завершении сессии пользователя по таймауту,
 * либо явного выхода.
 */
public class UserSessionClosedEvent implements Event {
	private final User user;
	private final HttpSessionEvent httpSessionEvent;
	
	public UserSessionClosedEvent(User user, HttpSessionEvent httpSessionEvent) {
		this.user = user;
		this.httpSessionEvent = httpSessionEvent;
	}

	public User getUser() {
		return user;
	}

	public HttpSessionEvent getHttpSessionEvent() {
		return httpSessionEvent;
	}
}
