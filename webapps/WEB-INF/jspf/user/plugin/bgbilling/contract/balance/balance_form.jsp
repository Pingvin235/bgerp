<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="balanceForm" value="${u:uiid()}"/>

<form action="/user/plugin/bgbilling/proto/balance.do" id="${balanceForm}" class="in-mb1-all">
	<input type="hidden" name="action" value="${form.param.action}" />
	<input type="hidden" name="billingId" value="${form.param.billingId}" />
	<input type="hidden" name="contractId" value="${form.param.contractId}" />

	<c:set var="sendForm" value="$$.ajax.load(this.form, $('#${balanceForm}').parent());"/>

	<c:set var="saveCommand" value="${sendForm}"/>

	Период c
	<ui:date-time paramName="dateFrom" value="${tu.format(frd.dateFrom, 'dd.MM.yyyy')}"/>
	по
	<ui:date-time paramName="dateTo" value="${tu.format(frd.dateTo, 'dd.MM.yyyy')}"/>

	<c:set var="contractInfo" value="${frd.contractInfo}"/>
	<button type="button" class="btn-white ml1 mr1"
			onclick="this.form.dateFrom.value='${tu.format(contractInfo.dateFrom, 'ymd')}'; this.form.dateTo.value='${tu.format(contractInfo.dateTo, 'ymd')}'; ${sendForm}">
			Весь период договора
	</button>

	<div style="display: inline-block;" class="in-mr1">
		<c:set var="action" value="paymentList"/>
		<c:set var="title" value="Приход"/>
		<%@ include file="balance_form_button.jsp"%>

		<c:set var="action" value="accountList"/>
		<c:set var="title" value="Наработка"/>
		<%@ include file="balance_form_button.jsp"%>

		<c:set var="action" value="chargeList"/>
		<c:set var="title" value="Расход"/>
		<%@ include file="balance_form_button.jsp"%>

		<c:set var="action" value="balance"/>
		<c:set var="title" value="Баланс"/>
		<%@ include file="balance_form_button.jsp"%>

		<c:set var="action" value="balanceDetail"/>
		<c:set var="title" value="Баланс дет."/>
		<%@ include file="balance_form_button.jsp"%>
	</div>
</form>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function()
	{
		$('#${contractTreeId} tr.balance').each( function()
		{
			$(this).attr( "onclick", $(this).attr( "onclick" ).replace( /&dateFrom=[\d\.]*&dateTo=[\d\.]*/, "&dateFrom=${dateFrom}&dateTo=${dateTo}" ) );
			$('#${contractTreeId} #balanceMonth').text( "${tu.format( contractInfo.balanceDate, 'MMMM Y' )}" );
			$('#${contractTreeId} #balanceIn').text( '${contractInfo.balanceIn}' );
			$('#${contractTreeId} #balancePayment').text( '${contractInfo.balancePayment}' );
			$('#${contractTreeId} #balanceAccount').text( '${contractInfo.balanceAccount}' );
			$('#${contractTreeId} #balanceCharge').text( '${contractInfo.balanceCharge}' );
			$('#${contractTreeId} #balanceOut').text( '${contractInfo.balanceOut}' );
		})
	})
</script>

<%-- TODO: Рекомендуемая сумма! --%>