<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<p:check action="org.bgerp.action.admin.AppAction:restart">
	<html:form action="/admin/app">
		<input type="hidden" name="method" value="restart"/>
		<%@ include file="run_restart_button.jsp"%>
	</html:form>
</p:check>