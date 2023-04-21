<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<jsp:include page="balance_form.jsp"/>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="createUrl" value="/user/plugin/bgbilling/proto/balance.do">
	<c:param name="action" value="balanceEditor" />
	<c:param name="billingId" value="${form.param.billingId}" />
	<c:param name="contractId" value="${form.param.contractId}" />
	<c:param name="item" value="contractCharge" />
	<c:param name="returnUrl" value="${form.requestUrl}" />
</c:url>
<button class="btn-green" title="Добавить расход" type="button" onclick="$$.ajax.load('${createUrl}', $('#${uiid}').parent())">+</button>

<table class="data mt1" width="100%" id="${uiid}">
	<tr>
		<td></td>
		<td>Дата</td>
		<td>Сумма</td>
		<td nowrap="nowrap">Тип расхода</td>
		<td width="100%">Комментарий</td>
		<td nowrap="nowrap">Время изменения</td>
		<td>Пользователь</td>
	</tr>
	<c:forEach var="charge" items="${form.response.data.list}">
		<tr>
			<c:url var="url" value="/user/plugin/bgbilling/proto/balance.do">
				<c:param name="action" value="balanceEditor"/>
				<c:param name="item" value="contractCharge" />
				<c:param name="billingId" value="${form.param.billingId}" />
				<c:param name="contractId" value="${form.param.contractId}" />
				<c:param name="id" value="${charge.getId()}"/>
				<c:param name="returnUrl" value="${form.requestUrl}" />
			</c:url>
			<c:set var="editCommand" value="$$.ajax.load('${url}', $('#${uiid}').parent())"/>

			<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/balance.do">
				<c:param name="action" value="deleteCharge"/>
				<c:param name="billingId" value="${form.param.billingId}" />
				<c:param name="contractId" value="${form.param.contractId}" />
				<c:param name="chargeId" value="${charge.getId()}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}',$('#${uiid}').parent())"/>
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td>${tu.format( charge.date, 'ymd' )}</td>
			<td>${charge.sum}</td>
			<td nowrap="nowrap">${charge.type}</td>
			<td>${charge.comment}</td>
			<td nowrap="nowrap">${tu.format( charge.lastChangeTime, 'ymdhms')}</td>
			<td nowrap="nowrap">${charge.user}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="columnTitle" value="Тип расхода"/>
<%@ include file="sub_item_list.jsp"%>