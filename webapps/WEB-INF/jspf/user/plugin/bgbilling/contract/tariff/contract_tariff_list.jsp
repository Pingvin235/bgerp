<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
	<c:param name="method" value="getContractTariff"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
	<c:param name="showUsed" value="1"/>
	<c:param name="useFilter" value="1"/>
</c:url>

<ui:button type="add" onclick="$$.ajax.load('${url}', $('#${uiid}').parent());"/>
<table class="data hl" id="${uiid}">
	<tr>
		<td width="30"></td>
		<td>Позиция</td>
		<td>Название</td>
		<td nowrap="nowrap">Период действия</td>
		<td width="100%">Комментарий</td>
	</tr>

	<c:forEach var="tariff" items="${frd.tariffList}">
		<tr>
			<c:url var="eUrl" value="${url}">
				<c:param name="id" value="${tariff.getId()}"/>
			</c:url>
			<c:url var="delUrl" value="/user/plugin/bgbilling/proto/contractTariff.do">
				<c:param name="method" value="deleteСontractTariff"/>
				<c:param name="contractId" value="${form.param.contractId}"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="id" value="${tariff.id}"/>
			</c:url>
			<td nowrap="nowrap">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${eUrl}', $('#${uiid}').parent())"/>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${delUrl}').done(() => $$.ajax.load('${form.requestUrl}',$('#${uiid}').parent()))"/>
			</td>
			<td nowrap="nowrap" align="center">${tariff.position}</td>
			<td nowrap="nowrap">${tariff.title}</td>
			<td nowrap="nowrap">${tu.formatPeriod( tariff.dateFrom, tariff.dateTo, 'ymd')}</td>
			<td>${tariff.comment}</td>
		</tr>
	</c:forEach>
</table>