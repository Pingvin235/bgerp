<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:url var="baseUrl" value="/user/plugin/grpl/process.do">
		<c:param name="id" value="${process.id}"/>
	</c:url>

	<c:set var="boards" value="${ctxPluginManager.pluginMap.grpl.getConfig(ctxSetup).getBoards(processType.id)}"/>

	<c:forEach var="board" items="${boards}">
		<c:url var="url" value="${baseUrl}">
			<c:param name="boardId" value="${board.id}"/>
		</c:url>

		$tabs.tabs('add', '${url}', '${board.title}');
	</c:forEach>
</u:sc>
