<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${uri.startsWith('/open/process/queue')}">
		<c:set var="config" value="${ctxSetup.getConfig('org.bgerp.action.open.ProcessQueueAction$Config')}"/>
		<c:set var="name" value="${su.substringBefore(su.substringAfterLast(uri, '/'), '#')}"/>

		<c:set var="queueId" value="${config.getOpenQueueId(name)}"/>
		<c:if test="${not empty queueId}">
			<c:import url="/open/process/queue.do?method=show&id=${queueId}"/>
		</c:if>
	</c:when>
	<c:when test="${uri.startsWith('/open/process')}">
		<c:import url="/open/process.do?method=show&id=${u.getOpenId(uri)}&secret=${secret}"/>
	</c:when>
</c:choose>
