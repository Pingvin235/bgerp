<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="balanceForm" value="${u:uiid()}"/>

<form action="/user/plugin/bgbilling/proto/balance.do" id="${balanceForm}" class="in-mb1-all in-inline-block">
	<input type="hidden" name="method" value="${form.param.method}" />
	<input type="hidden" name="billingId" value="${form.param.billingId}" />
	<input type="hidden" name="contractId" value="${form.param.contractId}" />

	<c:set var="sendForm" value="$$.ajax.load(this.form, $('#${balanceForm}').parent());"/>

	<c:set var="saveCommand" value="${sendForm}"/>

	<div>
		Период c
		<ui:date-time name="dateFrom" value="${form.param.dateFrom}"/>
		по
		<ui:date-time name="dateTo" value="${form.param.dateTo}"/>

		<c:set var="contractInfo" value="${frd.contractInfo}"/>
		<button type="button" class="btn-white ml1 mr1"
				onclick="this.form.dateFrom.value='${tu.format(contractInfo.dateFrom, 'ymd')}'; this.form.dateTo.value='${tu.format(contractInfo.dateTo, 'ymd')}'; ${sendForm}">
				Весь период договора
		</button>
	</div>

	<div class="in-mr1">
		<c:set var="method" value="paymentList"/>
		<c:set var="title" value="Приход"/>
		<%@ include file="balance_form_button.jsp"%>

		<c:set var="method" value="accountList"/>
		<c:set var="title" value="Наработка"/>
		<%@ include file="balance_form_button.jsp"%>

		<c:set var="method" value="chargeList"/>
		<c:set var="title" value="Расход"/>
		<%@ include file="balance_form_button.jsp"%>

		<c:set var="method" value="balance"/>
		<c:set var="title" value="Баланс"/>
		<%@ include file="balance_form_button.jsp"%>

		<c:set var="method" value="balanceDetail"/>
		<c:set var="title" value="Баланс дет."/>
		<%@ include file="balance_form_button.jsp"%>
	</div>
</form>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function () {
		<c:set var="locale" value="${u:newInstance1('java.util.Locale', 'ru')}"/>
		<c:set var="format" value="${u:newInstance2('java.text.SimpleDateFormat', 'LLLL Y', locale)}"/>
		$('#${contractTreeId} #balanceMonth').text("${format.format(contractInfo.balanceDate)}");
		$('#${contractTreeId} #balanceIn').text('${contractInfo.balanceIn}');
		$('#${contractTreeId} #balancePayment').text('${contractInfo.balancePayment}');
		$('#${contractTreeId} #balanceAccount').text('${contractInfo.balanceAccount}');
		$('#${contractTreeId} #balanceCharge').text('${contractInfo.balanceCharge}');
		$('#${contractTreeId} #balanceOut').text('${contractInfo.balanceOut}');
	})
</script>

<%-- TODO: Рекомендуемая сумма! --%>