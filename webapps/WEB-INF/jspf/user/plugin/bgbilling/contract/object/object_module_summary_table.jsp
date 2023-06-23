<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data">
	<tr>
		<td>Модуль</td>
		<td>Сущность</td>
		<td>Период</td>
		<td>Комментарий</td>
	</tr>

	<c:forEach var="data" items="${form.response.data.moduleInfo.moduleDataList}">
		<tr>
			<td align="center">${data.module}</td>
			<td>${data.data}</td>
			<td align="center">${data.period}</td>
			<td>${data.comment}</td>
		</tr>
	</c:forEach>
</table>