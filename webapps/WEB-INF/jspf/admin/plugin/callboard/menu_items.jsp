<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="${l.l('Организация работ')}" icon="ti-agenda">
	<ui:menu-item title="${l.l('Типы работ')}" href="admin/callboard/work"
		action="/admin/plugin/callboard/work:workTypeList" />

	<ui:menu-item title="${l.l('Шаблоны смен')}" href="admin/callboard/shift"
		action="/admin/plugin/callboard/work:shiftList" />
</ui:menu-group>