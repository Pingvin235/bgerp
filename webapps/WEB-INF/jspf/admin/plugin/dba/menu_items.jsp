<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="DBA" icon="ti-server">
	<ui:menu-item title="Database" href="admin/dba/db"
		action="org.bgerp.plugin.svc.dba.action.DatabaseAction:null"
		command="/admin/plugin/dba/db.do" />
</ui:menu-group>
