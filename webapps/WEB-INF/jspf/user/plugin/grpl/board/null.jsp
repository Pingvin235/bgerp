<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="boards" value="${frd.boards}"/>

<!-- search over boardId -->
<c:set var="boardId" value="${form.id}"/>
<c:if test="${not (boardId gt 0)}">
	<c:set var="boardId" value="${ctxUser.personalizationMap.grplBoardLastSelected}"/>
</c:if>
<c:if test="${not (boardId gt 0)}">
	<c:set var="boardId" value="${u.getFirst(boards.values()).id}"/>
</c:if>

<c:set var="uiid" value="${u:uiid()}"/>

<form action="${form.requestURI}" id="${uiid}" class="in-ml05">
	<input type="hidden" name="method" value="show"/>

	<ui:combo-single
		hiddenName="id"
		value="${boardId}"
		list="${boards.values()}"
		widthTextValue="20em"
		prefixText="${l.l('Board')}:"
		onSelect="$$.ajax.loadContent(this.form, this)"/>

	<ui:date-time paramName="dateFrom" value="${form.param.dateFrom}"/>

	<ui:date-time paramName="dateTo" value="${form.param.dateTo}"/>
</form>

<shell:state moveSelector="#${uiid}"/>

<shell:title text="${l.l('Group Plan')}"/>

<c:if test="${boardId gt 0}">
	<c:url var="url" value="${form.requestURI}">
		<c:param name="method" value="show"/>
		<c:param name="id" value="${boardId}"/>
		<c:param name="dateFrom" value="${form.param.dateFrom}"/>
		<c:param name="dateTo" value="${form.param.dateTo}"/>
	</c:url>
	<c:remove var="form"/>
	<c:import url="${url}"/>
</c:if>
