<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
	<c:param name="method" value="getPersonalTariff"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<button type="button" class="btn-green" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())">+</button>

<table class="data mt1" width="100%" id="${uiid}">
	<tr>
		<td width="30"></td>
		<td nowrap="nowrap">Позиция</td>
		<td nowrap="nowrap">Период</td>
		<td width="100%">Название</td>
	</tr>

	<c:forEach var="personalTariff" items="${frd.personalTariffList}">
		<tr>
			<c:url var="eUrl" value="${url}">
				<c:param name="id" value="${personalTariff.id}"/>
			</c:url>
			<c:set var="editCommand" value="$$.ajax.load('${eUrl}', $('#${uiid}').parent())"/>

			<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/contractTariff.do">
				<c:param name="method" value="deletePersonalTariff"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="id" value="${personalTariff.id}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent())"/>
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>

			<td align="center" nowrap="nowrap">${personalTariff.pos}</td>
			<td nowrap="nowrap">${tu.formatPeriod( personalTariff.date1, personalTariff.date2, 'ymd' )}</td>
			<td width="100%">${personalTariff.title}</td>
		</tr>
	</c:forEach>
</table>