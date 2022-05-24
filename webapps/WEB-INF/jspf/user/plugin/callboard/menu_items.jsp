<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group ltitle="Организация работ" icon="ti-agenda">
	<ui:menu-item ltitle="Календарь рабочих дней" href="callboard/calendar"
		action="org.bgerp.plugin.pln.callboard.action.WorkAction:workDaysCalendarList"
		command="/user/plugin/callboard/work.do?action=workDaysCalendarList" />

	<ui:menu-item ltitle="График дежурств" href="callboard"
		action="org.bgerp.plugin.pln.callboard.action.WorkAction:callboardGet"
		command="/user/plugin/callboard/work.do?action=callboardGet" />

	<ui:menu-item ltitle="План работ" href="callboard/workplan"
		action="org.bgerp.plugin.pln.callboard.action.WorkAction:planGet"
		command="/user/plugin/callboard/work.do?action=planGet" />
</ui:menu-group>