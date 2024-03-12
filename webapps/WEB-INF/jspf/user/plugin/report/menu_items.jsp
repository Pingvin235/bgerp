<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="${l.l('Отчёты')}" icon="ti-stats-up" actionFactory="org.bgerp.plugin.report.action.ReportActionBase$Factory">
	<ui:menu-item title="${l.l('JSP Отчёты')}" href="report"
		action="org.bgerp.plugin.report.action.ReportAction:null"
		command="/user/plugin/report/report.do" />
</ui:menu-group>
