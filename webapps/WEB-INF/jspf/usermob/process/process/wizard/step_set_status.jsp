<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<c:set var="tableId" value="${uiid}"/>
	<c:set target="${form.response.data}" property="process" value="${wizardData.process}"/>
	
	<%@ include file="/WEB-INF/jspf/user/process/process/process_status.jsp"%>
</div>
