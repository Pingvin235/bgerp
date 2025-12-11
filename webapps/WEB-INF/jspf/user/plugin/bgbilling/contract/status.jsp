<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<ul>
		<li><a href="#tabs-1">Статус</a></li><%--
	--%><li><a href="#tabs-2">История изменений [${frd.statusLog.size()}]</a></li>
	</ul>
	<div id="tabs-1">
		<html:form action="${form.requestURI}">
			<html:hidden property="billingId"/>
			<html:hidden property="contractId"/>
			<input type="hidden" name="method" value="updateStatus"/>

			<table style="width: 100%;">
				<tr class="in-pr05">
					<td>С даты:</td>
					<td class="nowrap">
						<ui:date-time name="dateFrom" value="${script.dateFrom}"/>
						по дату:
						<ui:date-time name="dateTo" value="${script.dateTo}"/>
					</td>
					<td width="100%" class="pl05">
						<ui:combo-single list="${frd.availableStatusList}" hiddenName="statusId" style="width: 100%;"/>
					</td>
				</tr>

				<c:set var="saveCommand">$$.ajax.post(this.form).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))</c:set>
				<tr class="in-pt05 in-pr05">
					<td>Комментарий:</td>
					<td>
						<input name="comment" onkeypress="if( enterPressed( event ) ){ ${saveCommand}; return false; }" style="width: 100%"/>
					</td>
					<td>
						<button type="button" type="button" class="btn-grey ml05" style="float: right;" onclick="${saveCommand}">Изменить</button>
					</td>
				</tr>
			</table>
		</html:form>

		<table class="data mt1 hl">
			<tr>
				<td>Период</td>
				<td>Статус</td>
				<td width="100%">Комментарий</td>
			</tr>
			<c:forEach var="status" items="${frd.statusList}">
				<tr>
					<td class="nowrap">${tu.formatPeriod(status.dateFrom, status.dateTo, 'ymd')}</td>
					<td class="nowrap" >${status.status}</td>
					<td>${status.comment}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${not empty frd.statusFutureTasks}">
			<h2>Задачи на смену статуса</h2>

			<table class="data mt1 hl">
				<tr>
					<td>Период</td>
					<td>Статус</td>
					<td>Статус задачи</td>
					<td>Комментарий</td>
					<td>Создана</td>
					<td>Создал</td>
				</tr>
				<c:forEach var="item" items="${frd.statusFutureTasks}">
					<tr>
						<td class="nowrap">${tu.formatPeriod(item.dateFrom, item.dateTo, 'ymd')}</td>
						<td>${item.status}</td>
						<td>${item.taskStatus.title}</td>
						<td>${item.comment}</td>
						<td>${tu.format(item.createTime, 'ymdhms')}</td>
						<td>${item.user}</td>
					</tr>
				</c:forEach>
			</table>
		</c:if>
	</div>
	<div id="tabs-2">
		<table class="data hl">
			<tr>
				<td>Период</td>
				<td>Статус</td>
				<td>Время</td>
				<td>Пользователь</td>
				<td width="100%">Комментарий</td>
			</tr>
			<c:forEach var="item" items="${frd.statusLog}">
				<tr>
					<td class="nowrap">${tu.formatPeriod(item.dateFrom, item.dateTo, 'ymd')}</td>
					<td class="nowrap">${item.status}</td>
					<td class="nowrap">${tu.format(item.date, 'ymdhms')}</td>
					<td class="nowrap">${item.user}</td>
					<td>${item.comment}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</div>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$("#${uiid}").tabs();
	$('#${contractTreeId} #treeTable td#status').text('${contractInfo.status}');
</script>