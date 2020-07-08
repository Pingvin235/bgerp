<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="Организация работ">
	<jsp:attribute name="subitems">
	
		<ui:menu-item title="Календарь рабочих дней" href="work/calendar"
			action="ru.bgcrm.struts.action.admin.WorkAction:workDaysCalendarList"
			command="/admin/work.do?action=workDaysCalendarList" />
	
		<ui:menu-item title="Типы работ" href="admin/work/type"
			action="ru.bgcrm.struts.action.admin.WorkAction:workTypeList"
			command="/admin/work.do?action=workTypeList" />
	
		<ui:menu-item title="Шаблоны смен" href="admin/shift/type"
			action="ru.bgcrm.struts.action.admin.WorkAction:shiftList"
			command="/admin/work.do?action=shiftList" />
	
		<ui:menu-item title="График дежурств" href="work/callboard"
			action="ru.bgcrm.struts.action.admin.WorkAction:callboardGet"
			command="/admin/work.do?action=callboardGet" />
	
		<ui:menu-item title="План работ" href="work/plan"
			action="ru.bgcrm.struts.action.WorkAction:planGet"
			command="/user/work.do?action=planGet" />
	</jsp:attribute>
</ui:menu-group>