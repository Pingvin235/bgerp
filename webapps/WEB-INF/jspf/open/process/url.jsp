<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${uri.startsWith('/open/process/queue')}">
		<c:set var="config" value="${u:getConfig(ctxSetup, 'org.bgerp.action.open.ProcessQueueAction$Config')}"/>
		<c:set var="name" value="${su.substringBefore(su.substringAfterLast(uri, '/'), '#')}"/>

		<c:set var="queueId" value="${config.getOpenQueueId(name)}"/>
		<c:if test="${not empty queueId}">
			<c:import url="/open/process/queue.do?action=show&id=${queueId}"/>
		</c:if>
	</c:when>
	<c:when test="${uri.startsWith('/open/process')}">
		<c:set var="userId" value="${su.substringBefore(su.substringAfterLast(uri, '/'), '#')}"/>
		<c:import url="/open/process.do?action=show&id=${userId}"/>
	</c:when>
</c:choose>
