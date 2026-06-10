<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-group title="${l.l('Reports')}" icon="ti-stats-up" actionFactory="org.bgerp.plugin.report.action.ReportActionBase$Factory">
	<ui:menu-item title="${l.l('JSP Reports')}" href="report" action="/user/plugin/report/report:null" />
</ui:menu-group>
