<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="plugin" value="${ctxPluginManager.pluginMap.report}" />
<c:set var="allowedReports" value="${form.response.data.allowedReports}" />

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="config" value="${u:getConfig(ctxSetup, 'org.bgerp.plugin.report.model.Config')}"/>

<ui:combo-single id="${uiid}" hiddenName="reportId" 
	map="${config.reportMap}" list="${config.reportList}" available="${allowedReports}"
	widthTextValue="300px">
	<jsp:attribute name="valuesHtml">
		<li value="-1">-- ${l.l('выберите отчёт')} --</li>
	</jsp:attribute>
	<jsp:attribute name="onSelect">
		if ($hidden.val()) 
			$$.ajax.load('/user/plugin/report/report.do?action=get&reportId=' + $hidden.val(), $$.shell.$content(this));
	</jsp:attribute>
</ui:combo-single>

<shell:title ltext="Отчёты"/>
<shell:state moveSelector="#${uiid}"/>