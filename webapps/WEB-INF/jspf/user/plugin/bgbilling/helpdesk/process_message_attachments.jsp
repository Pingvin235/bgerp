<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${messageType.getClass().name.endsWith('MessageTypeHelpDesk')}">
	<c:set var="billingId" value="${messageType.billingId}"/>

	<c:forEach var="item" items="${message.attachList}" varStatus="status">
		<c:url var="url" value="/user/plugin/bgbilling/proto/helpdesk.do">
			<c:param name="action" value="getAttach"/>
			<c:param name="processId" value="${message.processId}"/>
			<c:param name="billingId" value="${billingId}"/>
			<c:param name="id" value="${item.id}"/>
			<c:param name="title" value="${item.title}"/>
		</c:url>
		<a href="${url}">${item.title}</a><c:if test="${not status.last}">,</c:if>
	</c:forEach>
</c:if>