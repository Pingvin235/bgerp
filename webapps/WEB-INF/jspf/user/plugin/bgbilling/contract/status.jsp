<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/bgbilling/proto/contract" styleId="${uiid}">
	<html:hidden property="billingId"/>
	<html:hidden property="contractId"/>
	<input type="hidden" name="action" value="updateStatus"/>

	<table style="width: 100%;">
		<tr class="in-pr05">
			<td>С даты:</td>
			<td nowrap="nowrap">
				<ui:date-time paramName="dateFrom" value="${script.dateFrom}"/>
				по дату:
				<ui:date-time paramName="dateTo" value="${script.dateTo}"/>
			<td>
			<td width="100%" class="pl05">
				<ui:combo-single list="${frd.availableStatusList}" hiddenName="statusId" style="width: 100%;"/>
			</td>
			<td></td>
		</tr>

		<c:set var="saveCommand">$$.ajax.post(this.form).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))</c:set>
		<tr class="in-pt05 in-pr05">
			<td>Комментарий:</td>
			<td colspan="3">
				<input name="comment" onkeypress="if( enterPressed( event ) ){ ${saveCommand}; return false; }" style="width: 100%"/>
			</td>
			<td>
				<button type="button" type="button" class="btn-grey ml05" onclick="${saveCommand}">Изменить</button>
			</td>
		</tr>
	</table>
</html:form>

<table class="data mt1" style="width:100%;">
	<tr class="header">
		<td>Период</td>
		<td>Статус</td>
		<td width="100%">Комментарий</td>
	</tr>

	<c:forEach var="status" items="${frd.statusList}">
		<tr>
			<td nowrap="nowrap">${tu.formatPeriod( status.dateFrom, status.dateTo, 'ymd' )}</td>
			<td nowrap="nowrap" >${status.status}</td>
			<td>${status.comment}</td>
		</tr>
	</c:forEach>
</table>

<h2>История изменения статуса</h2>

<table class="data">
	<tr class="header">
		<td>Период</td>
		<td>Статус</td>
		<td>Время</td>
		<td>Пользователь</td>
		<td width="100%">Комментарий</td>
	</tr>

	<c:forEach var="item" items="${frd.statusLog}">
		<tr>
			<td nowrap="nowrap">${tu.formatPeriod( item.dateFrom, item.dateTo, 'ymd' )}</td>
			<td nowrap="nowrap">${item.status}</td>
			<td nowrap="nowrap">${tu.format( item.time, 'ymdhms' )}</td>
			<td nowrap="nowrap">${item.user}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function()
	{
		$('#${contractTreeId} #treeTable td#status').text( '${contractInfo.status}' );
	})
</script>