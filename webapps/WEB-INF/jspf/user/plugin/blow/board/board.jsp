<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="boardsConf" value="${form.response.data.boardsConf}"/>
<ui:combo-single id="${uiid}" 
	value="${form.id}"
	widthTextValue="220px"
	prefixText="${l.l('План')}:"
	list="${form.response.data.boardsConf.boards}"
	onSelect="$$.ajax.load('/user/plugin/blow/board.do?action=show&id=' + $hidden.val(), $$.shell.$content()); 
			  history.replaceState(history.state, null, '/user/blow/board#' + $hidden.val());"/>

<shell:state moveSelector="#${uiid}"/>

<shell:title text="${l.l('Blow board')}"/>

<c:choose>
	<c:when test="${form.id gt 0}">
		<script>
		$(function () {
			$$.ajax.load('/user/plugin/blow/board.do?action=show&id=${form.id}', $$.shell.$content());
		})
		</script>
	</c:when>
	<c:otherwise>
		<c:set var="boardId" value="${ctxUser.personalizationMap['blowBoardLastSelected']}"/>
		<c:if test="${boardId gt 0}">
			<script>
			$(function () {
				$$.shell.contentLoad("/user/blow/board#${boardId}", true);
			})
			</script>
		</c:if>
	</c:otherwise>
</c:choose>