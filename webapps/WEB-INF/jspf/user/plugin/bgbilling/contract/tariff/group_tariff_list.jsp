<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
	<c:param name="method" value="getContractTariffGroup"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<ui:button type="add" onclick="$$.ajax.load('${url}', $('#${uiid}').parent());"/>

<table class="data mt1 hl" id="${uiid}">
	<tr>
		<td width="30"></td>
		<td nowrap="nowrap">Группа тарифов</td>
		<td nowrap="nowrap">Период действия</td>
		<td width="100%">Комментарий</td>
	</tr>

	<c:forEach var="tariffGroup" items="${frd.tariffGroupList}">
		<tr>
			<c:url var="eUrl" value="${url}">
				<c:param name="id" value="${tariffGroup.getId()}"/>
			</c:url>

			<c:url var="delUrl" value="/user/plugin/bgbilling/proto/contractTariff.do">
				<c:param name="method" value="deleteContractTariffGroup"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="id" value="${tariffGroup.getId()}"/>
			</c:url>
			<td nowrap="nowrap">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${eUrl}', $('#${uiid}').parent())"/>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${delUrl}').done(() => $$.ajax.load('${form.requestUrl}',$('#${uiid}').parent()))"/>
			</td>
			<td nowrap="nowrap">${tariffGroup.getTitle()}</td>
			<td nowrap="nowrap">${tu.formatPeriod( tariffGroup.dateFrom, tariffGroup.dateTo, 'ymd' )}</td>
			<td>${tariffGroup.getComment()}</td>
		</tr>
	</c:forEach>
</table>