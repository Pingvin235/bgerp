<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="invoice" value="${frd.invoice}"/>
<c:set var="invoiceCustomer" value="${frd.invoiceCustomer}"/>
<c:set var="invoiceCustomerParam" value="${frd.invoiceCustomerParam}"/>
<c:set var="process" value="${frd.process}"/>
<c:set var="processParam" value="${frd.processParam}"/>
<c:set var="customer" value="${frd.customer}"/>
<c:set var="customerParam" value="${frd.customerParam}"/>

<html>
	<head>
		<%@ include file="style/common.jsp"%>
		<%@ include file="style/eu.jsp"%>
	</head>
	<body>
		<div style="height: 25mm; max-height: 25mm;">
			<%-- significantly increases file size, think about a public URL --%>
			<c:set var="logo" value="${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.logo')].base64EncodedImgSrc}"/>
			<c:if test="${not empty logo}">
				<img src="${logo}" alt="Logo" style="height: 100%; float: right;"/>
			</c:if>
		</div>

		<div class="in-table-cell" style="height: 40mm; margin-top: 25mm;">
			<div style="width: 100%;">
				${invoiceCustomer.title} -
				<c:set var="addr" value="${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.address')].valueTitle.replace(',', ' -')}"/>
				${addr}
				<br/><br/>
				${customer.title}<br/>
				<c:set var="addr" value="${customerParam[ctxSetup.getInt('invoice:param.customer.address')].valueTitle}"/>
				${addr.replace(",", "<br/>")}
			</div>
			<div style="white-space: nowrap;">
				Date:
				${tu.format(invoice.createdTime, 'ymd')}
			</div>
		</div>

		<div class="bold"  style="font-size: 1.5em; margin-top: 10mm;">
			INVOICE NO. ${invoice.number}
		</div>

		<table style="margin-top: 10mm;">
			<tr class="bold">
				<td>Pos.</td>
				<td>Description</td>
				<td class="right">Qty.</td>
				<td class="right">Price</td>
				<td class="right">Total</td>
			</tr>
			<c:forEach var="item" items="${invoice.positions}" varStatus="status">
				<tr>
					<td>${status.count}</td>
					<td>${item.title}</td>
					<td class="right">${item.quantity}</td>
					<td class="right">${item.price}</td>
					<td class="right">${item.amount} &euro;</td>
				</tr>
			</c:forEach>
			<tr class="bold">
				<td colspan="4" class="right">Total</td>
				<td class="right">
					${invoice.amount} &euro;
				</td>
			</tr>
		</table>

		<div style="flex-grow: 1; margin-top: 15mm;">
			${u:htmlEncode(invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.invoice.footer')].valueTitle)}
		</div>

		<div class="right">
			${invoiceCustomer.title}<br/>
			${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.bank.title')].valueTitle}<br/>
			IBAN: ${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.bank.iban')].valueTitle}<br/>
			BIC: ${invoiceCustomerParam[ctxSetup.getInt('invoice:param.customer.bank.bic')].valueTitle}<br/>
		</div>
	</body>
</html>

