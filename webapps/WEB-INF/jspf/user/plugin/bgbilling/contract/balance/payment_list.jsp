<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="balance_form.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="createUrl" value="${form.requestURI}">
	<c:param name="method" value="balanceEditor" />
	<c:param name="billingId" value="${form.param.billingId}" />
	<c:param name="contractId" value="${form.param.contractId}" />
	<c:param name="item" value="contractPayment" />
	<c:param name="returnUrl" value="${form.requestUrl}" />
</c:url>

<ui:button type="add" title="Добавить платёж" onclick="$$.ajax.load('${createUrl}', $('#${uiid}').parent())"/>
<c:set var="cashcheck" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[form.param.billingId].pluginSet.contains( 'ru.bitel.bgbilling.plugins.cashcheck' ) }"/>

<table class="data hl mt1" id="${uiid}">
	<tr>
		<td></td>
		<td>Дата</td>
		<td>Сумма</td>
		<td nowrap="nowrap">Тип платежа</td>
		<td width="100%">Комментарий</td>
		<td nowrap="nowrap">Время изменения</td>
		<td>Пользователь</td>
		<c:if test="${cashcheck}">
			<td>Действие</td>
		</c:if>
	</tr>
	<c:forEach var="payment" items="${frd.list}" varStatus="varStatus">
		<tr>
			<td nowrap="nowrap">
				<c:url var="url" value="${form.requestURI}">
					<c:param name="method" value="balanceEditor"/>
					<c:param name="item" value="contractPayment" />
					<c:param name="billingId" value="${form.param.billingId}" />
					<c:param name="contractId" value="${form.param.contractId}" />
					<c:param name="id" value="${payment.id}"/>
					<c:param name="cashcheck" value="${cashcheck}"/>
					<c:param name="returnUrl" value="${form.requestUrl}" />
				</c:url>
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())"/>

				<c:url var="url" value="${form.requestURI}">
					<c:param name="method" value="deletePayment"/>
					<c:param name="billingId" value="${form.param.billingId}" />
					<c:param name="contractId" value="${form.param.contractId}" />
					<c:param name="paymentId" value="${payment.id}"/>
				</c:url>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${url}').done(() => $$.ajax.load('${form.requestUrl}',$('#${uiid}').parent()))"/>
			</td>
			<td>${tu.format( payment.date, 'ymd')}</td>
			<td>${payment.sum}</td>
			<td nowrap="nowrap">${payment.type}</td>
			<td>${payment.comment}</td>
			<td nowrap="nowrap">${tu.format( payment.lastChangeTime, 'ymdhms' )}</td>
			<td nowrap="nowrap">${payment.user}</td>
			<c:if test="${cashcheck}">
				<td nowrap="nowrap">
					<c:set var="printCheckForm" value="${form.param.billingId}-${form.param.contractId}-printCheck-form"/>
					<button class="btn-white btn-small" type="button" style="width:100%"
							onclick="$('#${printCheckForm} input[name=paymentId]').val('${payment.id}');
									 $('#${printCheckForm} input[name=clientCash]').val('${payment.sum}');
									 $('#${printCheckForm}' ).dialog( 'open' );">Чек</button>
				</td>
			</c:if>
		</tr>
	</c:forEach>
</table>

<c:set var="columnTitle" value="Тип платежа"/>
<%@ include file="sub_item_list.jsp"%>

<%-- диалог печати чека платежа по кнопке "чек" и при закрытии редактора платежа --%>
<c:if test="${cashcheck}">
	<%@ include file="print_check.jsp"%>
</c:if>