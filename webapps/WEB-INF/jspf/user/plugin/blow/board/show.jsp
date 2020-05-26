<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="board" scope="request" value="${form.response.data.board}"/>
<c:if test="${not empty board}">
	<c:set var="uiid" value="${u:uiid()}"/>	

	<%@ include file="filters.jsp"%>	
		
	<table id="${uiid}" class="data" style="width: 100%;">
		<tr class="head">
			<c:forEach var="column" items="${board.executors}">
				<td>${column.title}<%@ include file="executor_count.jsp"%></td>
			</c:forEach>
		</tr>
		
		<%@ include file="table_data.jsp"%>	
	</table>
	
	<c:set var="uiidRcMenu" value="${u:uiid()}"/>
	<ul style="display: none; z-index: 2000;" id="${uiidRcMenu}">
		<li id="create"><a>${l.l('Новый процесс')}</a></li>
		<li id="cut"><a>${l.l('Вырезать')}</a></li>
		<li id="paste" style="display: none;"><a>${l.l('Вставить')}</a></li>
		<li id="free" style="display: none;"><a>${l.l('Отделить')}</a></li>
		<li id="merge" style="display: none;"><a>${l.l('Слить')}</a></li>
	</ul>
	
	<script>
		$(function () {
			$$.blow.initTable($('#${uiid}'), $('#${uiidRcMenu}'));
			
			$('#content > #blow-board').data('onShow', function () {
				$$.ajax.load('/user/plugin/blow/board.do?action=show&id=${form.id}', $$.shell.$content());
			});

			history.replaceState(history.state, null, '/user/blow/board#${form.id}');
		})
	</script>

	<c:if test="${not empty board.config.openUrl}">
		<shell:title>
			<jsp:attribute name="text">
				<a target='_blank' href='/open/blow/${board.config.openUrl}' title='${l.l('Открытый интерфейс')}'>O</a>
				${l.l('Blow план')}
			</jsp:attribute>
		</shell:title>
	</c:if>
</c:if>
