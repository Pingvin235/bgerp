<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="invoice" value="${form.response.data.invoice}"/>
<c:set var="invoiceCustomer" value="${form.response.data.invoiceCustomer}"/>
<c:set var="invoiceCustomerParam" value="${form.response.data.invoiceCustomerParam}"/>
<c:set var="process" value="${form.response.data.process}"/>
<c:set var="processParam" value="${form.response.data.processParam}"/>
<c:set var="customer" value="${form.response.data.customer}"/>
<c:set var="customerParam" value="${form.response.data.customerParam}"/>

<html>
	<head>
		<style>
		body {
			height: 297mm;
			width: 210mm;
			padding: 30mm 20mm;
			/* to centre page on screen*/
			margin-left: auto;
			margin-right: auto;
			font: 14px Arial, Geneva CY, Kalimati, Geneva, sans-serif;
		}
		table {
			width: 100%;
			border-collapse: collapse;
			font: inherit;
			font-size: 100%;
		}
		td {
			border: thin solid black;
			padding: 2mm;
			vertical-align: top;
		}
		.bottom {
			display: inline-block;
			bottom: 0;
			position: absolute;
		}
		.small {
			font-size: .9em;
		}
		.no-border-top {
			border-top: none;
		}
		.no-border-bottom {
			border-bottom: none;
		}
		.va-bottom {
			vertical-align: bottom;
		}
		.in-table-cell {
			display: table-row;
		}
		.in-table-cell > * {
			display: table-cell;
		}
		.center {
			text-align: center;
		}
		.right {
			text-align: right;
		}
		.bold {
			font-weight: bold;
		}
		</style>
	</head>
	<body>
		${invoiceCustomer.title}<br/></br>
		${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.address')].valueTitle}
		<%-- table width: 185 mm --%>
		<table style="margin-top: 30mm;">
			<tr>
				<td colspan="4" width="60%" class="no-border-bottom">
					${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.bank.title')].valueTitle}
				</td>
				<td width="8%">БИК</td>
				<td style="no-border-bottom">${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.bank.bic')].valueTitle}</td>
			</tr>
			<tr>
				<td colspan="4" class="no-border-top small va-bottom">Банк получателя</td>
				<td>Сч. №</td>
				<td class="no-border-top">${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.bank.corr.account')].valueTitle}</td>
			</tr>
			<tr>
				<td width="6%">ИНН</td>
				<td width="23%">${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.inn')].valueTitle}</td>
				<td width="6%">КПП</td>
				<td>${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.kpp')].valueTitle}</td>
				<td rowspan="3">Сч. №</td>
				<td rowspan="3">${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.bank.account')].valueTitle}</td>
			</tr>
			<tr>
				<td colspan="4" class="no-border-bottom">${invoiceCustomer.title}</td>
			</tr>
			<tr>
				<td colspan="4" class="small no-border-top">Получатель</td>
			</tr>
		</table>

		<div style="margin-top: 10mm; margin-bottom: 7mm; font-size: 1.2em; font-weight: bold; text-align: center;">
			Счёт на оплату № ${invoice.number} от ${tu.format(invoice.createdTime, 'ymd')}
		</div>

		<div class="in-table-cell">
			<div style="width: 22mm">Поставщик:</div>
			<div>${invoiceCustomer.title}</div>
		</div>
		<div class="in-table-cell">
			<div style="width: 22mm">Покупатель:</div>
			<div>
				${customer.title},
				ИНН ${customerParam[ctxSetup.getInt('invoice:param.customer.ru.inn')].valueTitle},
				КПП ${customerParam[ctxSetup.getInt('invoice:param.customer.ru.kpp')].valueTitle}
			</div>
		</div>

		<table style="margin-top: 12mm;">
			<tr class="center">
				<td width="0">№</td>
				<td width="54%">Товары (работы, услуги)</td>
				<td>Кол-во</td>
				<td>Ед.</td>
				<td>Цена</td>
				<td>Сумма</td>
			</tr>
			<c:forEach var="item" items="${invoice.positions}" varStatus="status">
				<tr>
					<td>${status.count}</td>
					<td>${item.title}</td>
					<td class="right">${item.quantity}</td>
					<td class="center">${item.unit}</td>
					<td class="right">${item.price}</td>
					<td class="right">${item.amount}</td>
				</tr>
			</c:forEach>
		</table>

		<div class="in-table-cell right bold" style="margin-top: 5mm; font-size: 1.1em;">
			<div style="width: 100%;">Итого к оплате:</div>
			<div style="min-width: 25mm;">${invoice.amount}</div>
		</div>
		<div class="in-table-cell right" style="font-size: 1.1em;">
			<div style="width: 100%;">В том числе НДС:</div>
			<div style="min-width: 25mm;">Без НДС</div>
		</div>

		<div style="margin-top: 7mm; border-bottom: 1mm solid black; font-size: 1.2em;">
			Всего к оплате: ${invoice.amount} рублей.
		</div>
		<div style="margin-top: 11mm;">
			На основании договора № ${process.id} от ${processParam[ctxSetup.getInt('invoice:param.process.contract.date')].valueTitle}
		</div>
		<div style="margin-top: 9mm; display: table;">
			<div class="in-table-cell">
				<div style="min-width: 22mm; padding-bottom: 2mm;">Поставщик</div>
				<div style="min-width: 75mm;" class="center">${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.sign.post')].valueTitle}</div>
				<div style="min-width: 40mm;" class="center">
					<%-- значительно увеличивает размер файла, при необходимости заменить на публичный URL --%>
					<c:set var="sign" value="${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.sign')].base64EncodedImgSrc}"/>
					<c:if test="${not empty sign}">
						<img src="${sign}" alt="Подпись" style="position: relative; top: 5mm;"/>
					</c:if>
				</div>
				<div style="min-width: 5mm;"></div>
				<div style="width: 100%;" class="center">${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.sign.name')].valueTitle}</div>
			</div>
			<div class="in-table-cell" style="font-size: 0.9em;">
				<div></div>
				<div style="border-top: 1px solid black;" class="center">должность</div>
				<div style="border-top: 1px solid black;" class="center">подпись</div>
				<div></div>
				<div style="border-top: 1px solid black;" class="center">расшифровка подписи</div>
			</div>
		</div>
		<div>
			<%-- значительно увеличивает размер файла, при необходимости заменить на публичный URL --%>
			<c:set var="stamp" value="${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.ru.stamp')].base64EncodedImgSrc}"/>
			<c:if test="${not empty stamp}">
				<img src="${stamp}" alt="Печать" style="position: relative; left: 3cm; top: -15mm;"/>
			</c:if>
		</div>
	</body>
</html>

