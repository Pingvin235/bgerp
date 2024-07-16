<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<c:url var="url" value="/user/process.do">
		<c:param name="method" value="processStatusEdit"/>
		<c:param name="id" value="${wizardData.process.id}"/>
		<c:param name="returnUrl" value="${reopenProcessUrl}"/>
		<c:param name="returnChildUiid" value="${uiid}"/>
	</c:url>
	<c:import url="${url}"/>
</div>
