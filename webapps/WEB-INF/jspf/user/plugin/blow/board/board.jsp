<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="boardId" value="${form.id}"/>
<c:if test="${not (boardId gt 0)}">
	<c:set var="boardId" value="${ctxUser.personalizationMap['blowBoardLastSelected']}"/>
</c:if>

<c:set var="boardsConf" value="${form.response.data.boardsConf}"/>
<ui:combo-single id="${uiid}" 
	value="${boardId}"
	widthTextValue="220px"
	prefixText="${l.l('План')}:"
	list="${form.response.data.boardsConf.boards}"
	onSelect="$$.blow.boardShow($hidden.val())"/>

<shell:state moveSelector="#${uiid}"/>

<shell:title text="${l.l('Blow план')}"/>

<c:if test="${boardId gt 0}">
	<c:url var="url" value="/user/plugin/blow/board.do">
		<c:param name="action" value="show"/>
		<c:param name="id" value="${boardId}"/>
	</c:url>
	<c:import url="${url}"/>
</c:if>
