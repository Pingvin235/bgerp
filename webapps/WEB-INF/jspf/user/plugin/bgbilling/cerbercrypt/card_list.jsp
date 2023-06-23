<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<%--
<c:url var="url" value="plugin/bgbilling/proto/npay.do">
	<c:param name="action" value="serviceGet"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<button type="button" class="btn-green mb1" title="Добавить абонплату" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())">+</button>
--%>

<table class="data" id="${uiid}">
	<tr>
		<td>Карта</td>
		<td>Период</td>
		<td>Подписка через Web</td>
		<td>Комментарий</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<%--
			<c:url var="editUr" value="${url}">
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:set var="editCommand" value="$$.ajax.load('${editUr}', $('#${uiid}').parent())"/>

			<c:url var="deleteAjaxUrl" value="plugin/bgbilling/proto/npay.do">
				<c:param name="action" value="serviceDelete"/>
				<c:param name="contractId" value="${form.param.conractId}"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="moduleId" value="${form.param.moduleId}"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}',$('#${uiid}').parent())"/>

			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			 --%>
			<td>${item.number}</td>
			<td nowrap="nowrap">${tu.format( item.dateFrom, 'ymd' )} - ${tu.format( item.dateTo, 'ymd' )}</td>
			<td>${tu.format(item.subscrDate, 'ymd')}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>