<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="baseUrl" value="/user/plugin/bgbilling/proto/contract.do">
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<c:url var="url" value="${baseUrl}">
	<c:param name="method" value="serviceEdit"/>
</c:url>

<button class="btn-green mb1" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())">+</button>

<table class="data hl" id="${uiid}">
	<tr>
		<td></td>
		<td>Услуга</td>
		<td>Период</td>
		<td>Комментарий</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td nowrap="nowrap">
				<u:sc>
					<c:url var="url" value="${baseUrl}">
						<c:param name="method" value="serviceEdit"/>
						<c:param name="id" value="${item.id}"/>
					</c:url>
					<c:set var="editCommand" value="$$.ajax.load('${url}', $('#${uiid}').parent())"/>

					<c:url var="deleteAjaxUrl" value="${baseUrl}">
						<c:param name="method" value="serviceDelete"/>
						<c:param name="id" value="${item.id}"/>
					</c:url>
					<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent())"/>
					<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				</u:sc>
			</td>
			<td nowrap="nowrap">${item.serviceTitle}</td>
			<td nowrap="nowrap">${tu.formatPeriod( item.dateFrom, item.dateTo, 'ymd' )}</td>
			<td width="100%">${item.comment}</td>
		</tr>
	</c:forEach>
</table>