<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
	<c:param name="action" value="getContractTariff"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
	<c:param name="showUsed" value="1"/>
	<c:param name="useFilter" value="1"/>
</c:url>

<button type="button" class="btn-green mb1" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())">+</button>

<table class="data" id="${uiid}">
	<tr>
		<td width="30"></td>
		<td>Позиция</td>
		<td>Название</td>
		<td nowrap="nowrap">Период действия</td>
		<td width="100%">Комментарий</td>
	</tr>

	<c:forEach var="tariff" items="${form.response.data.tariffList}">
		<tr>
			<c:url var="eUrl" value="${url}">
				<c:param name="id" value="${tariff.getId()}"/>
			</c:url>
			<c:set var="editCommand" value="$$.ajax.load('${eUrl}', $('#${uiid}').parent())"/>

			<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/contractTariff.do">
				<c:param name="action" value="deleteСontractTariff"/>
				<c:param name="contractId" value="${form.param.contractId}"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="id" value="${tariff.id}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent())"/>
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td nowrap="nowrap" align="center">${tariff.position}</td>
			<td nowrap="nowrap">${tariff.title}</td>
			<td nowrap="nowrap">${tu.formatPeriod( tariff.dateFrom, tariff.dateTo, 'ymd')}</td>
			<td>${tariff.comment}</td>
		</tr>
	</c:forEach>
</table>