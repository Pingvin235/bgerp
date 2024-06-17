<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data">
	<tr>
		<td>Устройство</td>
		<td>Сервис</td>
		<td>IP</td>
		<td>С ном./на ном.</td>
		<td>Начало</td>
		<td>Статус</td>
		<td>Состояние</td>
	</tr>
	<c:forEach var="item" items="${stepData.sessions}">
		<tr>
			<td>${item.deviceTitle}</td>
			<td>${item.serviceTitle}</td>
			<td>${item.ip}</td>
			<td>${item.fromNumberToNumberAsString}</td>
			<td>${item.sessionStartAsString}</td>
			<td>${item.statusName}</td>
			<td>${item.devStateTitle}</td>
		</tr>
	</c:forEach>
</table>