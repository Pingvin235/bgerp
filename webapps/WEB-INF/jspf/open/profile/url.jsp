<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${uri.startsWith('/open/profile')}">
	<c:set var="userId" value="${su.substringBefore(su.substringAfterLast(uri, '/'), '#')}"/>
	<c:import url="/open/profile.do?action=show&id=${userId}"/>
</c:if>
