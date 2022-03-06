<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group ltitle="Календарь" icon="ti-calendar">
	<ui:menu-item ltitle="Баланс" href="calendar/balance"
		action="org.bgerp.plugin.calendar.action.ActionBalance:show"
		command="/user/plugin/calendar/balance.do?action=show"/>
</ui:menu-group>