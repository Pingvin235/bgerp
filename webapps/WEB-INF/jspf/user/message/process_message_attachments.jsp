<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:forEach var="item" items="${message.attachList}" varStatus="status">
	<c:url var="url" value="/user/file.do">
		<c:param name="id" value="${item.id}"/>
		<c:param name="title" value="${item.title}"/>
		<c:param name="secret" value="${item.secret}"/>
	</c:url>
	<a href="${url}" class="preview">${item.title}</a><c:if test="${not status.last}">, </c:if>
</c:forEach>