<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<table class="data hl" id="${uiid}">
	<tr>
		<td>Карта</td>
		<td>Период</td>
		<td>Подписка через Web</td>
		<td>Комментарий</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td>${item.number}</td>
			<td nowrap="nowrap">${tu.format(item.dateFrom, 'ymd')} - ${tu.format(item.dateTo, 'ymd')}</td>
			<td>${tu.format(item.subscrDate, 'ymd')}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>