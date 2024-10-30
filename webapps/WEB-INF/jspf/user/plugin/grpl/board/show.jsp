<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="menuUiid" value="${u:uiid()}"/>
<ui:popup-menu id="${menuUiid}"><%-- dynamically loaded --%></ui:popup-menu>

<c:set var="board" value="${frd.board}"/>
<c:if test="${not empty board}">
	<c:set var="uiid" value="${u:uiid()}"/>
	<table id="${uiid}" class="hdata hl-td">
		<tr class="header">
			<td class="min text-right">${l.l('Date')}</td>
			<c:forEach var="column" items="${board.columns.values()}">
				<td width="${board.columnWidth}%">${column.title}</td>
			</c:forEach>
		</tr>

		<c:set var="today" value="${u:newInstance0('java.util.Date')}"/>
		<c:set var="locale" value="${u:newInstance1('java.util.Locale', setup.lang)}"/>
		<c:set var="format" value="${u:newInstance2('java.text.SimpleDateFormat', 'EEE dd.MM', locale)}"/>

		<c:forEach var="row" items="${frd.rows}">
			<tr bg-date="${tu.format(row.date, 'ymd')}" class="in-va-top">
				<td class="header nowrap text-right
					${row.workingDay ? '' : ' grpl-non-working-day'}
					${tu.dateEqual(today, row.date) ? ' bold' : ''}
				">${format.format(row.date)}</td>
				<c:forEach var="columnId" items="${board.columns.keySet()}">
					<c:set var="cell" value="${row.getCell(columnId)}"/>
					<c:set var="group" value="${cell.group}"/>
					<td bg-column-id="${columnId}" class="${tu.dateBefore(row.date, today) ? 'grpl-past' : ''}">
						<div class="grpl-board-group">${group.title}</div>
						<c:forEach var="slot" items="${cell.slots}">
							<div class="grpl-board-process">
								<c:if test="${not empty slot.time}">(${slot.time})&nbsp;</c:if>
								<ui:process-link process="${slot.process}"/>
							</div>
						</c:forEach>
					</td>
				</c:forEach>
			</tr>
		</c:forEach>
	</table>

	<script>
		$(function () {
			$$.grpl.menuInit('${uiid}', '${menuUiid}', ${board.id}, '${form.requestURI}', '${form.requestUrl}');

			$('#content > #grpl-board').data('onShow', () => {
				$$.ajax.loadContent('${form.requestUrl}', $('#${uiid}'));
			});

			$$.shell.stateFragment(${board.id});
		});
	</script>
</c:if>



