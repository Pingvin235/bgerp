<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<c:set target="${form}" property="returnUrl" value="${reopenProcessUrl}"/>
	<c:set target="${form}" property="returnChildUiid" value="${uiid}"/>
	<c:set target="${frd}" property="process" value="${wizardData.process}"/>
	<%@ include file="/WEB-INF/jspf/user/process/process/editor_status.jsp"%>
</div>
