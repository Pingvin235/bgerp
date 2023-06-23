<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<table class="data" id="${uiid}">
	<tr>
		<td>Карта</td>
		<td>Период</td>
		<td>Пакет</td>
		<td>Статус</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td>${item.card}</td>
			<td nowrap="nowrap">${tu.format( item.dateFrom, 'ymd' )} - ${tu.format( item.dateTo, 'ymd' )}</td>
			<td>${item.packet}</td>
			<td>${item.status}</td>
		</tr>
	</c:forEach>
</table>