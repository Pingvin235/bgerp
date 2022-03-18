<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Редактирование счёта')}</h1>

<c:set var="invoice" value="${form.response.data.invoice}"/>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/invoice/invoice" styleId="${uiid}">
	<input type="hidden" name="action" value="update"/>
	<input type="hidden" name="id" value="${invoice.id}"/>
	<input type="hidden" name="processId" value="${invoice.processId}"/>
	<input type="hidden" name="typeId" value="${invoice.typeId}"/>
	<input type="hidden" name="dateFrom" value="${tu.format(invoice.dateFrom, 'ymd')}"/>

	<table class="data mt05">
		<tr>
			<td>ID</td>
			<td width="100%">${l.l('Наименование')}</td>
			<td>${l.l('Кол-во')}</td>
			<td>${l.l('Ед.')}</td>
			<td>${l.l('Сумма')}</td>
			<td>&nbsp;</td>
		</tr>
		<c:forEach var="item" items="${invoice.positions}">
			<tr>
				<td><input type="text" name="pos_id" value="${item.id}" disabled="true" size="10"/></td>
				<td><input type="text" name="pos_title" value="${item.title}" style="width: 100%;"/></td>
				<td><input type="text" name="pos_quantity" value="${item.quantity}" size="2"/></td>
				<td><input type="text" name="pos_unit" value="${item.unit}" size="3"/></td>
				<td><input type="text" name="pos_amount" value="${item.amount}" size="5"/></td>
				<td><ui:button type="del" onclick="$(this).closest('tr').remove()"/></td>
			</tr>
		</c:forEach>
		<tr>
			<td colspan="2">
				<ui:combo-single list="${form.response.data.positions}" style="width: 100%;"/>
			</td>
			<td><input type="text" name="add_quantity" value="1" onkeydown="return isNumberKey(event)" size="2"/></td>
			<td><input type="text" name="add_unit" value="${l.l('шт.')}" size="3"/></td>
			<td><input type="text" name="add_amount" value="0.00" onkeydown="return isNumberKey(event)" size="5"/></td>
			<td><ui:button type="add" onclick="$$.invoice.addPosition(this)"/></td>
		</tr>
	</table>

	<ui:form-ok-cancel loadReturn="$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent())" styleClass="mt1"/>
</html:form>