<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="uiid" value="${u:uiid()}"/>
	<div id="${uiid}">
		<c:set var="requestUrl" value="${reopenProcessUrl}"/>
		<c:set var="tableId" value="${uiid}"/>
		<c:set target="${frd}" property="process" value="${wizardData.process}"/>

		<%@ include file="/WEB-INF/jspf/user/process/process/process_description.jsp"%>
	</div>
</u:sc>