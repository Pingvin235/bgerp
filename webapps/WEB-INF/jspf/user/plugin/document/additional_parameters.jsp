<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="page" value="${ctxSetup.subKeyed('document:pattern.').get( form.param.patternId )['additionalParametersJsp']}"/>
<c:if test="${not empty page}">
	<jsp:include page="${page}"/>
</c:if>