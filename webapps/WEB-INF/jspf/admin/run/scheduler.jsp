<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<table id="${uiid}" class="hdata hl">
	<thead>
		<tr>
			<td rowspan="2">ID</td>
			<td rowspan="2">${l.l('Title')}</td>
			<td colspan="6">${l.l('Schedule')}</td>
			<td colspan="2">${l.l('Last Run')}</td>
			<td rowspan="2" width="1em">${l.l('Run')}</td>
		</tr>
		<tr>
			<td>${l.l('Enabled')}</td>
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
		<c:forEach var="item" items="${frd.scheduled}">
			<tr>
				<td>${item.id}</td>
				<td>${item.title}</td>
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
				<td>${tu.format(item.lastRunStart, 'ymdhms')}</td>
				<td>${item.lastRunDuration.toMillis()}</td>
				<td class="nowrap">
					<c:choose>
						<c:when test="${not empty item.notRunnableState}"><b>${l.l(item.notRunnableState)}</b></c:when>
						<c:otherwise>
							<p:check action="org.bgerp.action.admin.RunAction:schedulerRun">
								<form action="/admin/run.do">
									<input type="hidden" name="action" value="schedulerRun"/>
									<input type="hidden" name="id" value="${item.id}"/>
									<ui:toggle styleClass="btn-toggle-small" inputName="wait" title="${l.l('Wait of execution is done')}"/>
									<button type="button" class="btn-white btn-small icon ml05" onclick="
										$$.ajax.post(this).done(() => {
											$$.ajax.loadContent('${form.requestUrl}', this);
										})
									"><i class="ti-control-play"></i></button>
								</form>
							</p:check>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<shell:title text="${l.l('Scheduler')}"/>

<c:set var="state" value="${frd.error}"/>
<c:choose>
	<c:when test="${not empty state}">
		<shell:state error="${state}" help="kernel/setup.html#scheduler"/>
	</c:when>
	<c:otherwise>
		<c:set var="runningCount" value="${frd.runningCount}"/>
		<c:if test="${0 lt runningCount}">
			<c:set var="state">${l.l('Running')}: ${runningCount}</c:set>
		</c:if>
		<shell:state text="${state}" help="kernel/setup.html#scheduler"/>
	</c:otherwise>
</c:choose>

<script>
	$(function () {
		const $content = $('#${uiid}').parent();
		$content.data('onShow', function () {
			$$.ajax.load("${form.requestUrl}", $content);
		});
	});
</script>
