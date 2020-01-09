<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${message.incoming}">&gt;&gt;</c:when>
	<c:when test="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeNote'}"></c:when>
	<c:otherwise>&lt;&lt;</c:otherwise>
</c:choose>
