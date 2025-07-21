<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data hl">
	<tr>
		<td>Реквизит</td>
		<td>Значение</td>
		<td>Период</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td>${item.title}</td>
			<td>${item.value}</td>
			<td>${tu.formatPeriod( item.dateFrom, item.dateTo, 'ymd' )}</td>
		</tr>
	</c:forEach>
</table>