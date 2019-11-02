package ru.bgcrm.event.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.bgcrm.event.Event;

/**
 * Событие перед авторизацией пользователя.
 */
public class UserAuthenticatingEvent implements Event {
	private final HttpServletRequest httpServletRequest;
	private final HttpServletResponse httpServletResponse;

	public UserAuthenticatingEvent(HttpServletRequest request, HttpServletResponse response) {
		this.httpServletRequest = request;
		this.httpServletResponse = response;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}
}
