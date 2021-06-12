<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group ltitle="Организация работ" icon="ti-agenda">
	<ui:menu-item ltitle="Календарь рабочих дней" href="callboard/calendar"
		action="ru.bgcrm.struts.action.admin.WorkAction:workDaysCalendarList"
		command="/admin/work.do?action=workDaysCalendarList" />

	<ui:menu-item ltitle="График дежурств" href="callboard"
		action="ru.bgcrm.struts.action.admin.WorkAction:callboardGet"
		command="/admin/work.do?action=callboardGet" />

	<ui:menu-item ltitle="План работ" href="callboard/workplan"
		action="ru.bgcrm.struts.action.WorkAction:planGet"
		command="/user/work.do?action=planGet" />
</ui:menu-group>