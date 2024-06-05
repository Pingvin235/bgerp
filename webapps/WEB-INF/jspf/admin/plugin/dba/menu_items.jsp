<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="DBA" icon="ti-server">
	<ui:menu-item title="Database" href="admin/dba/db" action="org.bgerp.plugin.svc.dba.action.admin.DatabaseAction:null" />
	<ui:menu-item title="SQL Query" href="admin/dba/query" action="org.bgerp.plugin.svc.dba.action.admin.QueryAction:null" />
</ui:menu-group>
