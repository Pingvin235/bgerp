<%@ page contentType="text/plain; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="config" value="${u:getConfig(ctxSetup, 'ru.bgerp.plugin.blow.model.BoardsConfig')}"/>

else if ((m = href.match(/blow/)) != null) {
    let boardId = null;
    
    let hrefBeforeId = href;
    const pos = hrefBeforeId.indexOf("#");
    if (pos > 0)
    	hrefBeforeId = hrefBeforeId.substr(0, pos);

    if (0) {}
	<c:forEach var="board" items="${config.openBoards}">
		else if (hrefBeforeId.endsWith("${board.openUrl}"))
		   boardId = ${board.id};
	</c:forEach>
	
	if (boardId)
		url = "/open/plugin/blow/board.do?action=show&id=" + boardId;
}