<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${uri.startsWith('/open/party')}">
	<c:set var="secret" value="${su.substringAfterLast(uri, '/party/')}"/>
	<c:url var="url" value="/open/plugin/team/party.do">
		<c:if test="${not empty secret}">
			<c:param name="secret" value="${secret}"/>
		</c:if>
	</c:url>
	<c:import url="${url}"/>
</c:if>