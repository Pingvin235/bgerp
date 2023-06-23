<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="blow-board">
	<c:set var="boardConf" scope="request" value="${form.response.data.boardConf}"/>
	<c:set var="board" scope="request" value="${form.response.data.board}"/>
	<c:if test="${not empty board}">
		<c:set var="uiid" value="${u:uiid()}"/>
		<%@ include file="/WEB-INF/jspf/user/plugin/blow/board/filters.jsp"%>

		<table id="${uiid}" class="data">
			<tr class="head">
				<c:forEach var="column" items="${board.executors}">
					<td>
						<ui:user-link id="${column.id}"/>
						<%@ include file="/WEB-INF/jspf/user/plugin/blow/board/executor_count.jsp"%>
					</td>
				</c:forEach>
			</tr>
			<%@ include file="/WEB-INF/jspf/user/plugin/blow/board/table_data.jsp"%>
		</table>

		<script>
			$(function () {
				bgerp.blow.initTable($('#${uiid}'));
			})
		</script>
		<shell:title text="${boardConf.title}"/>
	</c:if>
</div>