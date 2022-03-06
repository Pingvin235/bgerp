<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${message.incoming}"><i class="ti-cloud-down"></i></c:when>
	<c:when test="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeNote'}"></c:when>
	<c:otherwise><i class="ti-cloud-up"></i></c:otherwise>
</c:choose>
