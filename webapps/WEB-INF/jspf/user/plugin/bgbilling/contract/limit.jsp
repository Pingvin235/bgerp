<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<html:form action="/user/plugin/bgbilling/proto/contract" onsubmit="return false;" style="white-space: nowrap; display: inline-block;">
		<input type="hidden" name="method" value="updateLimit"/>
		<html:hidden property="contractId"/>
		<html:hidden property="billingId"/>

		<input type="text" size="5" placeholder="Сумма" name="value" style="text-align: center;"/>
		/
		<input type="text" size="5" placeholder="Дней" name="period" style="text-align: center;"/>

		<input type="text" size="12" placeholder="Комментарий" name="comment" class="ml05"/>

		<button type="button" class="btn-grey ml1"
				onclick="$$.ajax.post(this.form).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))">Изменить</button>
	</html:form>

	<html:form action="/user/plugin/bgbilling/proto/contract" style="width: 100%;">
		<input type="hidden" name="method" value="limit"/>
		<html:hidden property="contractId"/>
		<html:hidden property="billingId"/>

		<ui:page-control nextCommand="; $$.ajax.load(this.form, $('#${uiid}').parent());"/>
	</html:form>
</div>

<table class="data hl">
	<tr>
		<td>Время</td>
		<td>Пользователь</td>
		<td>Лимит</td>
		<td>Дней</td>
		<td width="100%">Комментарий</td>
	</tr>

	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td nowrap="nowrap">${tu.format(item.time, 'ymdhms')}</td>
			<td nowrap="nowrap" >${item.user}</td>
			<td nowrap="nowrap" >${item.limit}</td>
			<td nowrap="nowrap" ><c:if test="${item.days gt 0}">${item.days}</c:if></td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>

<h2>Задания на автоматическую смену лимита</h2>

<table class="data hl">
	<tr>
		<td></td>
		<td>Дата</td>
		<td>Статус</td>
		<td>Пользователь</td>
		<td width="100%">Изменение лимита на</td>
	</tr>

	<c:forEach var="item" items="${frd.taskList}">
		<tr>
			<td>
				<c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
					<c:param name="method" value="deleteLimitTask"/>
					<c:param name="billingId" value="${form.param.billingId}"/>
					<c:param name="contractId" value="${form.param.contractId}"/>
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<c:set var="deleteAjaxUrl" value="${url}"/>
				<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent())"/>
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td nowrap="nowrap">${tu.format( item.date, 'ymd' )}</td>
			<td nowrap="nowrap">${item.status}</td>
			<td nowrap="nowrap">${item.user}</td>
			<td>${item.limitChange}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function()
	{
		$('#${contractTreeId} #treeTable td#limit').text( '${frd.limit}' );
	})
</script>