<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group ltitle="${l.l('Организация работ')}">
	<ui:menu-item ltitle="${l.l('Календарь рабочих дней')}" href="work/calendar"
		action="ru.bgcrm.struts.action.admin.WorkAction:workDaysCalendarList"
		command="/admin/work.do?action=workDaysCalendarList" />

	<ui:menu-item ltitle="${l.l('Типы работ')}" href="admin/work/type"
		action="ru.bgcrm.struts.action.admin.WorkAction:workTypeList"
		command="/admin/work.do?action=workTypeList" />

	<ui:menu-item ltitle="${l.l('Шаблоны смен')}" href="admin/shift/type"
		action="ru.bgcrm.struts.action.admin.WorkAction:shiftList"
		command="/admin/work.do?action=shiftList" />

	<ui:menu-item ltitle="График дежурств" href="work/callboard"
		action="ru.bgcrm.struts.action.admin.WorkAction:callboardGet"
		command="/admin/work.do?action=callboardGet" />

	<ui:menu-item ltitle="${l.l('План работ')}" href="work/plan"
		action="ru.bgcrm.struts.action.WorkAction:planGet"
		command="/user/work.do?action=planGet" />
</ui:menu-group>