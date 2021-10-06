<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table style="width: 100%;" class="data">
	<tr>
		<td>Реквизит</td>
		<td>Значение</td>
		<td>Период</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td>${item.title}</td>
			<td>${item.value}</td>
			<td>${tu.formatPeriod( item.dateFrom, item.dateTo, 'ymd' )}</td>
		</tr>
	</c:forEach>
</table>