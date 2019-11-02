<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
	
<c:url var="url" value="plugin/bgbilling/proto/contractTariff.do">
	<c:param name="action" value="getContractTariffGroup"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<button type="button" class="btn-green" onclick="openUrlToParent('${url}', $('#${uiid}') )">+</button>

<table class="data mt1" width="100%" id="${uiid}">
	<tr>
		<td width="30"></td>
		<td nowrap="nowrap">Группа тарифов</td>
		<td nowrap="nowrap">Период действия</td>
		<td width="100%">Комментарий</td>
	</tr>
	
	<c:forEach var="tariffGroup" items="${form.response.data.tariffGroupList}">
		<tr>
			<c:url var="eUrl" value="${url}">
				<c:param name="id" value="${tariffGroup.getId()}"/>
			</c:url>
			<c:set var="editCommand" value="openUrlToParent('${eUrl}', $('#${uiid}') )"/>
			
			<c:url var="deleteAjaxUrl" value="plugin/bgbilling/proto/contractTariff.do">
				<c:param name="action" value="deleteContractTariffGroup"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="id" value="${tariffGroup.getId()}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}', $('#${uiid}') )"/>
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td nowrap="nowrap">${tariffGroup.getTitle()}</td>
			<td nowrap="nowrap">${u:formatPeriod( tariffGroup.dateFrom, tariffGroup.dateTo, 'ymd' )}</td>
			<td>${tariffGroup.getComment()}</td>
		</tr>
	</c:forEach>
</table>