<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mb1 in-mr1">
	<c:forEach var="pair" items="${board.config.getFilterValues(board.items)}">
		<button class="btn-white" style="color:${pair.first.color};" bg-id="${pair.first.id}" onclick="$$.blow.toggleFilterHighlight($('#${uiid}'), $(this))">${pair.second}</button>
	</c:forEach>
</div>
