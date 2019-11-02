<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="boardsConf" value="${form.response.data.boardsConf}"/>
<ui:combo-single id="${uiid}" 
	value="${form.id}"
	widthTextValue="220px"
	prefixText="${l.l('План')}:"
	list="${form.response.data.boardsConf.boards}"
	onSelect="openUrlContent('/user/plugin/blow/board.do?action=show&id=' + $hidden.val()); 
			  history.replaceState(history.state, null, '/user/blow/board#' + $hidden.val());"/>

<script>
$(function () {
	const $state = $('#title > .status:visible > .wrap > .center');
	$state.find(">div").remove();
	$('#${uiid}').appendTo($state);						
})
</script>

<c:set var="title" value="${l.l('Blow board')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>

<c:choose>
	<c:when test="${form.id gt 0}">
		<script>
		$(function () {
			openUrlContent('/user/plugin/blow/board.do?action=show&id=${form.id}');
		})
		</script>
	</c:when>
	<c:otherwise>
		<c:set var="boardId" value="${ctxUser.personalizationMap['blowBoardLastSelected']}"/>
		<c:if test="${boardId gt 0}">
			<script>
			$(function () {
				bgerp.shell.contentLoad("/user/blow/board#${boardId}", true);
			})
			</script>
		</c:if>
	</c:otherwise>
</c:choose>