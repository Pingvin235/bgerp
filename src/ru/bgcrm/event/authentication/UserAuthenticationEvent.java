package ru.bgcrm.event.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.bgcrm.event.Event;
import ru.bgcrm.model.authentication.AuthenticationMode;
import ru.bgcrm.model.authentication.AuthenticationResult;
import ru.bgcrm.model.user.User;

/**
 * Событие после выполнения авторизации пользователя.
 */
public class UserAuthenticationEvent implements Event {
	private final User user;
	private final HttpServletRequest httpServletRequest;
	private final HttpServletResponse httpServletResponse;
	private final AuthenticationMode authenticationMode;
	private final AuthenticationResult authenticationResult;

	public UserAuthenticationEvent(User user, HttpServletRequest request, HttpServletResponse response,
			AuthenticationMode authenticationMode, AuthenticationResult authenticationResult) {
		this.user = user;
		this.httpServletRequest = request;
		this.httpServletResponse = response;
		this.authenticationMode = authenticationMode;
		this.authenticationResult = authenticationResult;
	}
	
	public User getUser() {
		return user;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}

	public AuthenticationMode getAuthenticationMode() {
		return authenticationMode;
	}

	public AuthenticationResult getAuthenticationResult() {
		return authenticationResult;
	}
}
