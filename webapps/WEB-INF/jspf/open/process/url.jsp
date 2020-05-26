<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${uri.startsWith('/open/process')}">
	<c:set var="userId" value="${su.substringBefore(su.substringAfterLast(uri, '/'), '#')}"/>
	<c:import url="/open/process.do?action=show&id=${userId}"/>
</c:if>
