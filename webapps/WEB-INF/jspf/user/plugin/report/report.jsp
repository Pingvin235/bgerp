<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="title" value="Отчеты"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>

<c:set var="plugin" value="${ctxPluginManager.pluginMap.report}" />
<c:set var="allowedReports" value="${form.response.data.allowedReports}" />
<c:set var="uiid" value="${u:uiid()}" />

<u:sc>
	<c:set var="id" value="reportsList" />

	<c:set var="valuesHtml">
		<li value="-1">не выбран отчет</li>
	</c:set>
	<c:set var="map" value="${plugin.reportMap}" />
	<c:set var="list" value="${plugin.reportList}" />
	<c:set var="hiddenName" value="reportId" />
	<c:set var="prefixText" value="Отчет:" />
	<c:set var="available" value="${allowedReports}" />
	<c:set var="onSelect">
		openUrlTo('empty.do?reportId='+$hidden.val()+'&forwardFile=/WEB-INF/jspf/user/plugin/report/report_load.jsp', $('#${uiid}') )
	</c:set>
	<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
</u:sc>



<div class="tableIndent" id="${uiid}"></div>
