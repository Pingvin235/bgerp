<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group ltitle="Организация работ" icon="ti-agenda">
	<ui:menu-item ltitle="Типы работ" href="admin/callboard/work"
		action="org.bgerp.plugin.pln.callboard.action.admin.WorkAction:workTypeList"
		command="/admin/plugin/callboard/work.do?action=workTypeList" />

	<ui:menu-item ltitle="Шаблоны смен" href="admin/callboard/shift"
		action="org.bgerp.plugin.pln.callboard.action.admin.WorkAction:shiftList"
		command="/admin/plugin/callboard/work.do?action=shiftList" />
</ui:menu-group>