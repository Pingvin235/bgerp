<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${priority lt 2}">
		<c:set var="color" value="white"/>
	</c:when>
	<c:when test="${(priority gt 1) and (priority lt 4)}">
		<c:set var="color" value="9AD78A"/>
	</c:when>
	<c:when test="${(priority gt 3) and (priority lt 6)}">
		<c:set var="color" value="FFF1A4"/>
	</c:when>
	<c:when test="${(priority gt 5) and (priority lt 9)}">
		<c:set var="color" value="FFBE7E"/>
	</c:when>
	<c:otherwise>
		<c:set var="color" value="FD7D89"/>
	</c:otherwise>
</c:choose>