<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="/user/plugin/bgbilling/proto/npay.do">
	<c:param name="action" value="serviceGet"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<ui:button type="add" styleClass="mb1" title="Добавить абонплату" onclick="$$.ajax.load('${url}', $('#${uiid}').parent(), {control: this})"/>

<table class="data hl" width="100%" id="${uiid}">
	<tr>
		<td width="30"></td>
		<td width="30%">Услуга</td>
		<td nowrap="1">Кол-во</td>
		<td>Период</td>
		<td width="30%">Объект</td>
		<td width="30%">Комментарий</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td nowrap="nowrap">
				<c:url var="editUrl" value="${url}">
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $('#${uiid}').parent(), {control: this})"/>

				<c:url var="deleteUrl" value="/user/plugin/bgbilling/proto/npay.do">
					<c:param name="action" value="serviceDelete"/>
					<c:param name="contractId" value="${form.param.conractId}"/>
					<c:param name="billingId" value="${form.param.billingId}"/>
					<c:param name="moduleId" value="${form.param.moduleId}"/>
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}', {control: this}).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))"/>
			</td>
			<td>${item.serviceTitle}</td>
			<td>${item.count}</td>
			<td nowrap="nowrap">${tu.format( item.dateFrom, 'ymd' )} - ${tu.format( item.dateTo, 'ymd' )}</td>
			<td>${item.objectTitle}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>