<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="balance_form.jsp"%>

<table class="data" width="100%">
	<tr>
		<td>Дата</td>
		<td>Сумма</td>
		<td>Тип</td>
		<td width="100%">Комментарий</td>
	</tr>
	<c:forEach var="detailBalance" items="${form.response.data.list}">
		<tr>
			<td nowrap="nowrap">${detailBalance.date}</td>
			<td nowrap="nowrap">${detailBalance.summa}</td>
			<td nowrap="nowrap">${detailBalance.type}</td>
			<td>${detailBalance.comment}</td>
		</tr>
	</c:forEach>
</table>