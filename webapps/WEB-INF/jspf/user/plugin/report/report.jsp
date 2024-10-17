<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="plugin" value="${ctxPluginManager.pluginMap.report}" />
<c:set var="allowedReports" value="${frd.allowedReports}" />

<c:set var="uiid" value="${u:uiid()}"/>

<ui:combo-single id="${uiid}" hiddenName="reportId"
	map="${config.reportMap}" list="${config.reportList}" available="${allowedReports}"
	widthTextValue="20em">
	<jsp:attribute name="valuesHtml">
		<li value="-1">-- ${l.l('выберите отчёт')} --</li>
	</jsp:attribute>
	<jsp:attribute name="onSelect">
		if (this.value)
			$$.ajax.loadContent('/user/plugin/report/report.do?method=get&reportId=' + this.value, this);
	</jsp:attribute>
</ui:combo-single>

<shell:title text="${l.l('Отчёты')}"/>
<shell:state moveSelector="#${uiid}"/>