<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group ltitle="Организация работ" icon="ti-agenda">
	<ui:menu-item ltitle="Типы работ" href="admin/callboard/work"
		action="ru.bgcrm.struts.action.admin.WorkAction:workTypeList"
		command="/admin/work.do?action=workTypeList" />

	<ui:menu-item ltitle="Шаблоны смен" href="admin/callboard/shift"
		action="ru.bgcrm.struts.action.admin.WorkAction:shiftList"
		command="/admin/work.do?action=shiftList" />
</ui:menu-group>