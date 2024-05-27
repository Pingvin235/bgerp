<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="${l.l('Организация работ')}" icon="ti-agenda">
	<ui:menu-item title="${l.l('Типы работ')}" href="admin/callboard/work"
		action="org.bgerp.plugin.pln.callboard.action.admin.WorkAction:workTypeList"
		command="/admin/plugin/callboard/work.do?method=workTypeList" />

	<ui:menu-item title="${l.l('Шаблоны смен')}" href="admin/callboard/shift"
		action="org.bgerp.plugin.pln.callboard.action.admin.WorkAction:shiftList"
		command="/admin/plugin/callboard/work.do?method=shiftList" />
</ui:menu-group>