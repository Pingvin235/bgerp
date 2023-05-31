<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>${l.l('Scheduled')}</h2>
<table class="hdata hl">
	<thead>
		<tr>
			<td rowspan="2">ID</td>
			<td rowspan="2">${l.l('Class')}</td>
			<td rowspan="2">${l.l('Enabled')}</td>
			<td colspan="5">${l.l('Schedule')}</td>
			<td colspan="2">${l.l('Last Execution')}</td>
		</tr>
		<tr>
			<td>${l.l('Minute')}</td>
			<td>${l.l('Hour')}</td>
			<td>${l.l('Day of Month')}</td>
			<td>${l.l('Month')}</td>
			<td>${l.l('Day of Week')}</td>
			<td>${l.l('Start Time')}</td>
			<td>${l.l('Duration (ms)')}</td>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="item" items="${form.response.data.scheduled}">
			<tr>
				<td>${item.id}</td>
				<td>${item.className}</td>
				<td>
					<c:choose>
						<c:when test="${item.enabled}"><b>${l.l('Yes')}</b></c:when>
						<c:otherwise>${l.l('No')}</c:otherwise>
					</c:choose>
				</td>
				<td>${item.minute}</td>
				<td>${item.hour}</td>
				<td>${item.dayOfMonth}</td>
				<td>${item.month}</td>
				<td>${item.dayOfWeek}</td>
				<td>${tu.format(item.lastExecutionStart, 'ymdhms')}</td>
				<td>${item.lastExecutionDuration.toMillis()}</td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<shell:title ltext="Scheduler"/>
<shell:state error="${form.response.data.error}" help="kernel/setup.html#scheduler"/>