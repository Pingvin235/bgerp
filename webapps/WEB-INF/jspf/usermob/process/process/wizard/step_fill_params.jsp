<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<c:set var="id" value="${stepData.objectId}"/>
	<c:set var="list" value="${stepData.values}"/>
	<c:set var="onlyData" value="1"/>
	<c:set target="${form}" property="requestUrl" value="${reopenProcessUrl}"/>
	
	<c:set var="tableId" value="${uiid}"/>
	
	<c:set var="paramsConfig" value="${stepData.step.config}"/>
	
	<%@ include file="/WEB-INF/jspf/parameter_list.jsp"%>
</div>
