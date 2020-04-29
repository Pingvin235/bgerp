<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${uri.startsWith('/open/blow')}">
	<c:set var="config" value="${u:getConfig(ctxSetup, 'ru.bgerp.plugin.blow.model.BoardsConfig')}"/>
	<c:set var="boardName" value="${su:substringBefore(su:substringAfterLast(uri, '/'), '#')}"/>

	<c:forEach var="board" items="${config.openBoards}">
		<c:if test="${boardName eq board.openUrl}">
			<c:import url="/open/plugin/blow/board.do?action=show&id=${board.id}"/>
		</c:if>
	</c:forEach>
</c:if>