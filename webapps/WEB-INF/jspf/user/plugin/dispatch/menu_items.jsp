<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="${l.l('Рассылки')}">
	<ui:menu-item title="${l.l('Рассылки')}" href="dispatch"
		action="ru.bgcrm.plugin.dispatch.action.DispatchAction:dispatchList"
		command="/user/plugin/dispatch/dispatch.do?action=dispatchList" />

	<ui:menu-item title="${l.l('Сообщения рассылок')}" href="dispatch/message"
		action="ru.bgcrm.plugin.dispatch.action.DispatchAction:messageList"
		command="/user/plugin/dispatch/dispatch.do?action=messageList" />
</ui:menu-group>