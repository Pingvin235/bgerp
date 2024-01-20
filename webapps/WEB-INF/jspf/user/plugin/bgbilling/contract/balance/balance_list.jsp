<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="balance_form.jsp"%>

<table class="data">
	<tr>
		<td>Месяц, год</td>
		<td>Входящий остаток</td>
		<td>Приход</td>
		<td>Наработка</td>
		<td>Расход</td>
		<td>Исходящий остаток</td>
	</tr>
	<c:forEach var="balance" items="${frd.list}">
		<tr>
			<td align="center">${balance.month}</td>
			<td align="center">${balance.inputBalance}</td>
			<td align="center">${balance.payment}</td>
			<td align="center">${balance.account}</td>
			<td align="center">${balance.charge}</td>
			<td align="center">${balance.outputBalance}</td>
		</tr>
	</c:forEach>
</table>