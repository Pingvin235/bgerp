<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="board" scope="request" value="${frd.board}"/>
<c:if test="${not empty board}">
	<c:set var="uiid" value="${u:uiid()}"/>

	<div class="in-table-cell mb1">
		<%@ include file="filters.jsp"%>

		<p:check action="org.bgerp.plugin.pln.blow.action.BoardAction:search">
			<form action="${form.httpRequestURI}">
				<input type="hidden" name="method" value="search"/>
				<c:forEach var="id" items="${frd.processIds}">
					<input type="hidden" name="processId" value="${id}"/>
				</c:forEach>
				<ui:input-text name="filter" styleClass="ml1" value="${form.param['filter']}" placeholder="${l.l('Filter')}" size="40"
					title="${l.l('По тексту сообщения')}"
					onSelect="return $$.blow.search(this.form)"
				/>
			</form>
		</p:check>
	</div>

	<table id="${uiid}" class="data">
		<tr class="head">
			<c:forEach var="column" items="${board.executors}">
				<td>${column.title}<%@ include file="executor_count.jsp"%></td>
			</c:forEach>
		</tr>

		<%@ include file="table_data.jsp"%>
	</table>

	<c:set var="uiidRcMenu" value="${u:uiid()}"/>
	<ul style="display: none; z-index: 2000;" id="${uiidRcMenu}">
		<li id="create"><a>${l.l('rc.menu.create')}</a></li>
		<li id="cut"><a>${l.l('Вырезать')}</a></li>
		<li id="paste" style="display: none;"><a>${l.l('Вставить')}</a></li>
		<li id="free" style="display: none;"><a>${l.l('rc.menu.free')}</a></li>
		<li id="merge" style="display: none;"><a>${l.l('Слить')}</a></li>
	</ul>

	<script>
		$(function () {
			$$.blow.initTable($('#${uiid}'), $('#${uiidRcMenu}'));

			$('#content > #blow-board').data('onShow', function () {
				$$.ajax.loadContent('${form.httpRequestURI}?method=show&id=${form.id}', $('#${uiid}'));
			});

			$$.shell.stateFragment(${form.id});
		})
	</script>

	<c:if test="${not empty board.config.openUrl}">
		<shell:title>
			<jsp:attribute name="text">
				<a target='_blank' href='/open/blow/${board.config.openUrl}' title='${l.l('Open Interface')}'>O</a>
				${l.l('Blow план')}
			</jsp:attribute>
		</shell:title>
	</c:if>
</c:if>
