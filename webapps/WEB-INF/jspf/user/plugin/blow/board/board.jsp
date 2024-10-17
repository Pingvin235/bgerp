<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="showUrl" value="/user/plugin/blow/board.do">
	<c:param name="method" value="show"/>
</c:url>

<c:set var="boards" value="${frd.boards}"/>

<!-- search over boardId -->
<c:set var="boardId" value="${form.id}"/>
<c:if test="${not (boardId gt 0)}">
	<c:set var="boardId" value="${ctxUser.personalizationMap['blowBoardLastSelected']}"/>
</c:if>
<c:if test="${not (boardId gt 0)}">
	<c:set var="boardId" value="${u.getFirst(boards).id}"/>
</c:if>

<ui:combo-single id="${uiid}"
	value="${boardId}"
	widthTextValue="15em"
	prefixText="${l.l('План')}:"
	list="${frd.boards}"
	onSelect="$$.ajax.loadContent('${showUrl}&id=' + this.value, this)"/>

<shell:state moveSelector="#${uiid}"/>

<shell:title text="${l.l('Blow план')}"/>

<c:if test="${boardId gt 0}">
	<c:url var="url" value="${showUrl}">
		<c:param name="id" value="${boardId}"/>
	</c:url>
	<c:import url="${url}"/>
</c:if>
