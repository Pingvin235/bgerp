<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="${l.l('Организация работ')}" icon="ti-agenda">
	<ui:menu-item title="${l.l('Календарь рабочих дней')}" href="callboard/calendar"
		action="org.bgerp.plugin.pln.callboard.action.WorkAction:workDaysCalendarList" />

	<ui:menu-item title="${l.l('График дежурств')}" href="callboard"
		action="org.bgerp.plugin.pln.callboard.action.WorkAction:callboardGet" />

	<ui:menu-item title="${l.l('План работ')}" href="callboard/workplan"
		action="org.bgerp.plugin.pln.callboard.action.WorkAction:planGet" />
</ui:menu-group>