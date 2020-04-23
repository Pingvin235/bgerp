<%@ tag body-content="empty" pageEncoding="UTF-8" description="Ссылка на открытие процесса"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="Код процесса" required="true" type="java.lang.Integer"%>
<%@ attribute name="text" description="Опционально - текст ссылки, если не указан - то используется код процесса"%>

<a href="/user/process#${id}" onclick="$$.process.open(${id}); return false;"><%--
--%><c:choose>
		<c:when test="${not empty text}">${text}</c:when>
		<c:otherwise>${id}</c:otherwise>
	</c:choose><%--
--%></a>