<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}">
	<c:url var="url" value="/user/plugin/bgbilling/proto/contractTariff.do">
		<c:param name="action" value="tariffOptionEditor"/>
		<c:param name="contractId" value="${form.param.contractId}"/>
		<c:param name="billingId" value="${form.param.billingId}"/>
		<c:param name="returnUrl" value="${form.param.requestUrl}"/>
	</c:url>
	<button type="button" class="btn-green" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())">+</button>

	<h2>Текущие</h2>
	<table class="data">
		<tr>
			<td width="30"></td>
			<td width="100%">Название</td>
			<td nowrap="nowrap">Время активации</td>
			<td nowrap="nowrap">Время окончания</td>
			<td nowrap="nowrap">Стоимость активации</td>
		</tr>

		<c:forEach var="tariffOption" items="${frd.list}">
			<tr>
				<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/contractTariff.do">
					<c:param name="action" value="deleteTariffOption"/>
					<c:param name="contractId" value="${form.param.contractId}"/>
					<c:param name="billingId" value="${form.param.billingId}"/>
					<c:param name="optionId" value="${tariffOption.getId()}"/>
				</c:url>
				<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}', $('${uiid}').parent());"/>
				<td nowrap="nowrap">
					<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				</td>
				<td width="100%" >${tariffOption.optionTitle}</td>
				<td nowrap="nowrap" align="center">${tu.format( tariffOption.timeFrom, 'ymdhms' )}</td>
				<td nowrap="nowrap" align="center">${tu.format( tariffOption.timeTo, 'ymdhms' )}</td>
				<td align="center">${tariffOption.getSumma()}</td>
			</tr>
		</c:forEach>
	</table>

	<h2>История</h2>
	<table class="data">
		<tr>
			<td nowrap="nowrap">Название</td>
			<td nowrap="nowrap">Время активации</td>
			<td nowrap="nowrap">Время окончания</td>
			<td nowrap="nowrap">Стоимость активации</td>
		</tr>

		<c:forEach var="tariffOption" items="${frd.history}">
			<tr>
				<td width="100%">${tariffOption.optionTitle}</td>
				<td nowrap="nowrap" align="center">${tu.format( tariffOption.timeFrom, 'ymdhms' )}</td>
				<td nowrap="nowrap" align="center">${tu.format( tariffOption.timeTo, 'ymdhms' )}</td>
				<td>${tariffOption.summa}</td>
			</tr>
		</c:forEach>
	</table>
</div>