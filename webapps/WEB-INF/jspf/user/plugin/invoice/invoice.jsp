<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="invoice" value="${form.response.data.invoice}"/>

f

<%-- <table class="data mt05">
	<tr>
		<td>&nbsp;</td>
		<td>${l.l('Месяц')}</td>
		<td>${l.l('Сумма')}</td>
		<td>${l.l('Создан')}</td>
		<td>${l.l('Оплачен')}</td>
		<td>&nbsp;</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}" >
		<tr>
			<td>&nbsp;</td>
			<td>${tu.format(item.fromDate, 'yyyy.MM')}</td>
			<td>${item.summa}</td>
			<td>${tu.format(item.createdTime, 'ymdhm')}</td>
			<td>${tu.format(item.paymentDate, 'ymd')}</td>
			<td>
				<p:check action=""
				<c:url var=""
				<a href="">DOC
			</td>
		</tr>
	</c:forEach>
</table> --%>
