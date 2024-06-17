<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data">
	<tr>
		<td>Сервис</td>
		<td>Тип</td>
		<td>Пароль</td>
		<td>Период</td>
		<td>Статус</td>
		<td>Состояние</td>
	</tr>
	<c:forEach var="item" items="${stepData.services}">
		<tr>
			<td>${item.title}</td>
			<td>${item.typeTitle}</td>
			<td>${item.password}</td>
			<td>${tu.formatPeriod(item.dateFrom, item.dateTo, 'ymd')}</td>
			<td>${item.statusTitle}</td>
			<td>${item.devStateTitle}</td>
		</tr>
	</c:forEach>
</table>