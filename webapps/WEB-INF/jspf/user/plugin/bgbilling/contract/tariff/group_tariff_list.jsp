<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
	<c:param name="action" value="getContractTariffGroup"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<button type="button" class="btn-green" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())">+</button>

<table class="data mt1" width="100%" id="${uiid}">
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
			<c:set var="editCommand" value="$$.ajax.load('${eUrl}', $('#${uiid}').parent())"/>

			<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/contractTariff.do">
				<c:param name="action" value="deleteContractTariffGroup"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="id" value="${tariffGroup.getId()}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent())"/>
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td nowrap="nowrap">${tariffGroup.getTitle()}</td>
			<td nowrap="nowrap">${tu.formatPeriod( tariffGroup.dateFrom, tariffGroup.dateTo, 'ymd' )}</td>
			<td>${tariffGroup.getComment()}</td>
		</tr>
	</c:forEach>
</table>