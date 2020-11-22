<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="showUrl" value="/user/plugin/blow/board.do">
	<c:param name="action" value="show"/>
</c:url>

<c:set var="boardsConf" value="${form.response.data.boardsConf}"/>

<!-- search over boardId -->
<c:set var="boardId" value="${form.id}"/>
<c:if test="${not (boardId gt 0)}">
	<c:set var="boardId" value="${ctxUser.personalizationMap['blowBoardLastSelected']}"/>
</c:if>
<c:if test="${not (boardId gt 0)}">
	<c:set var="boardId" value="${u.getFirst(boardsConf.boards).id}"/>
</c:if>

<ui:combo-single id="${uiid}" 
	value="${boardId}"
	widthTextValue="220px"
	prefixText="${l.l('План')}:"
	list="${form.response.data.boardsConf.boards}"
	onSelect="$$.ajax.load('${showUrl}&id=' + $hidden.val(), $$.shell.$content())"/>

<shell:state moveSelector="#${uiid}"/>

<shell:title text="${l.l('Blow план')}"/>

<c:if test="${boardId gt 0}">
	<c:url var="url" value="${showUrl}">
		<c:param name="id" value="${boardId}"/>
	</c:url>
	<c:import url="${url}"/>
</c:if>
