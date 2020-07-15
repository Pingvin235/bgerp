<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="balance_form.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="createUrl" value="/user/plugin/bgbilling/proto/balance.do">
	<c:param name="action" value="balanceEditor" />
	<c:param name="billingId" value="${form.param.billingId}" />
	<c:param name="contractId" value="${form.param.contractId}" />
	<c:param name="item" value="contractPayment" />		
	<c:param name="returnUrl" value="${form.requestUrl}" />
</c:url> 	

<button type="button" class="btn-green" title="Добавить платёж" onclick="openUrlToParent('${createUrl}', $('#${uiid}') )">+</button>
<c:set var="cashcheck" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[form.param.billingId].pluginSet.contains( 'ru.bitel.bgbilling.plugins.cashcheck' ) }"/>
	
<table class="data mt1" width="100%" id="${uiid}">
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
	<c:forEach var="payment" items="${form.response.data.list}" varStatus="varStatus">
		<tr>
			<c:url var="url" value="/user/plugin/bgbilling/proto/balance.do">
				<c:param name="action" value="balanceEditor"/>
				<c:param name="item" value="contractPayment" />	
				<c:param name="billingId" value="${form.param.billingId}" />
				<c:param name="contractId" value="${form.param.contractId}" />
				<c:param name="id" value="${payment.id}"/>
				<c:param name="cashcheck" value="${cashcheck}"/>
				<c:param name="returnUrl" value="${form.requestUrl}" />
			</c:url>
			<c:set var="editCommand" value="openUrlToParent('${url}', $('#${uiid}') )"/>
			
			<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/balance.do">
				<c:param name="action" value="deletePayment"/>
				<c:param name="billingId" value="${form.param.billingId}" />
				<c:param name="contractId" value="${form.param.contractId}" />
				<c:param name="paymentId" value="${payment.id}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}',$('#${uiid}'))"/>
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td>${u:formatDate( payment.date, 'ymd')}</td>
			<td>${payment.sum}</td>
			<td nowrap="nowrap">${payment.type}</td>
			<td>${payment.comment}</td>
			<td nowrap="nowrap">${u:formatDate( payment.lastChangeTime, 'ymdhms' )}</td>
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