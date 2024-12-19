<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="DBA" icon="ti-server">
	<ui:menu-item title="Database" href="admin/dba/db" action="/admin/plugin/dba/db:null" />
	<ui:menu-item title="SQL Query" href="admin/dba/query" action="/admin/plugin/dba/query:null" />
</ui:menu-group>
