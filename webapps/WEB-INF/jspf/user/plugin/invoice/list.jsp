<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data mt05">
	<tr>
		<td>&nbsp;</td>
		<td>${l.l('Месяц')}</td>
		<td>${l.l('Сумма')}</td>
		<td>${l.l('Создан')}</td>
		<td>${l.l('Оплачен')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}" >
		<tr>
			<td></td>
			<td>${tu.format(item.fromDate, 'yyyy.MM')}</td>
			<td>${item.summa}</td>
			<td>${tu.format(item.createdTime, 'ymdhm')}</td>
			<td>${tu.format(item.paymentDate, 'ymd')}</td>
		</tr>
	</c:forEach>
</table>
