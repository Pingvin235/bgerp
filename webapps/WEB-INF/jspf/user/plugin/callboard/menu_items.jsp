<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="${l.l('Организация работ')}" icon="ti-agenda">
	<ui:menu-item title="${l.l('Календарь рабочих дней')}" href="callboard/calendar"
		action="/user/plugin/callboard/work:workDaysCalendarList" />

	<ui:menu-item title="${l.l('График дежурств')}" href="callboard"
		action="/user/plugin/callboard/work:callboardGet" />

	<ui:menu-item title="${l.l('План работ')}" href="callboard/workplan"
		action="/user/plugin/callboard/work:planGet" />
</ui:menu-group>