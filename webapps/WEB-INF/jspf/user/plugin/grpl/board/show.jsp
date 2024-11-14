<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="menuUiid" value="${u:uiid()}"/>
<ui:popup-menu id="${menuUiid}"><%-- dynamically loaded --%></ui:popup-menu>

<c:set var="dialogUiid" value="${u:uiid()}"/>
<form id="${dialogUiid}" action="${form.requestURI}" style="display: none;"><%-- dynamically loaded --%></form>

<c:set var="board" value="${frd.board}"/>
<c:if test="${not empty board}">
	<c:set var="uiid" value="${u:uiid()}"/>
	<table id="${uiid}" class="hdata grpl-board">
		<tr class="header">
			<td class="min text-right">${l.l('Date')}</td>
			<c:forEach var="column" items="${frd.columns}">
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
				">
					<c:choose>
						<c:when test="${not empty row.date}">${format.format(row.date)}</c:when>
						<c:otherwise><b>${l.l('QUEUE')}</b></c:otherwise>
					</c:choose>
				</td>
				<c:forEach var="column" items="${frd.columns}">
					<c:set var="cell" value="${row.getCell(column.id)}"/>
					<c:set var="group" value="${cell.group}"/>
					<td bg-column-id="${column.id}" class="${row.date ne null and tu.dateBefore(row.date, today) ? 'grpl-past' : ''}">
						<c:if test="${not empty group}">
							<div class="grpl-board-group">${group.title}</div>
						</c:if>
						<c:forEach var="slot" items="${cell.slots}">
							<c:choose>
								<c:when test="${not empty slot.process}">
									<div bg-process-id="${slot.process.id}" bg-duration="${slot.duration.toMinutes()}" id="${u:uiid()}" class="grpl-board-process" style="background-color: ${board.getProcessBackgroundColor(slot.process.statusId)};">
										<c:if test="${not empty slot.time}">(${slot.time})&nbsp;</c:if>
										<ui:process-link process="${slot.process}"/>
										<c:if test="${empty slot.time}">&nbsp;(${tu.format(slot.duration)})</c:if>
										<div class="grpl-board-process-description drop" style="display: none;">
											${slot.process.description}
										</div>
									</div>
								</c:when>
								<c:otherwise>
									<div bg-duration="${slot.duration.toMinutes()}" bg-time="${slot.time}" class="grpl-board-process-placement">${slot.time} - ${slot.timeTo}</div>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</td>
				</c:forEach>
			</tr>
		</c:forEach>
	</table>

	<script>
		$(function () {
			$$.grpl.menu('${uiid}', '${menuUiid}', ${board.id}, '${form.requestURI}', '${form.requestUrl}');

			$$.grpl.drag('${uiid}', '${dialogUiid}', ${board.id}, '${form.requestURI}', '${form.requestUrl}');

			$$.grpl.popup('${uiid}');

			$('#content > #grpl-board').data('onShow', () => {
				$$.ajax.loadContent('${form.requestUrl}', $('#${uiid}'));
			});

			$$.shell.stateFragment(${board.id});
		});
	</script>
</c:if>



