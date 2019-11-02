<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="plugin" value="${ctxPluginManager.pluginMap.report}" />
<c:set var="report" value="${plugin.reportMap[form.param.reportId]}"/>

<c:url var="url" value="/user/empty.do">
	<c:param name="forwardFile" value="${report.jspFile}"/>
</c:url>

<c:import url="${url}"/>
