<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
	<c:param name="action" value="getPersonalTariff"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<button type="button" class="btn-green" onclick="openUrlToParent('${url}', $('#${uiid}') )">+</button>

<table class="data mt1" width="100%" id="${uiid}">
	<tr>
		<td width="30"></td>
		<td nowrap="nowrap">${l.l('Позиция')}</td>
		<td nowrap="nowrap">${l.l('Период')}</td>
		<td width="100%">${l.l('Название')}</td>
	</tr>
	
	<c:forEach var="personalTariff" items="${form.response.data.personalTariffList}">
		<tr>
			<c:url var="eUrl" value="${url}">
				<c:param name="id" value="${personalTariff.id}"/>
			</c:url>
			<c:set var="editCommand" value="openUrlToParent('${eUrl}', $('#${uiid}') )"/>
			
			<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/contractTariff.do">
				<c:param name="action" value="deletePersonalTariff"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="id" value="${personalTariff.id}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}', $('#${uiid}') )"/>
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			
			<td align="center" nowrap="nowrap">${personalTariff.position}</td>
			<td nowrap="nowrap">${u:formatPeriod( personalTariff.dateFrom, personalTariff.dateTo, 'ymd' )}</td>
			<td width="100%">${personalTariff.title}</td>			
		</tr>
	</c:forEach>
</table>