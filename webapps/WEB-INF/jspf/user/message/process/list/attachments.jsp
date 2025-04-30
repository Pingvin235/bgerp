<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:forEach var="item" items="${message.attachList}" varStatus="status">
	<ui:file-link file="${item}"/><c:if test="${not status.last}">, </c:if>
</c:forEach>