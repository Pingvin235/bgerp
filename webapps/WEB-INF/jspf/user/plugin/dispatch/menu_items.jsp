<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="${l.l('Рассылки')}">
	<ui:menu-item title="${l.l('Рассылки')}" href="dispatch"
		action="/user/plugin/dispatch/dispatch:dispatchList" />

	<ui:menu-item title="${l.l('Сообщения рассылок')}" href="dispatch/message"
		action="/user/plugin/dispatch/dispatch:messageList" />
</ui:menu-group>