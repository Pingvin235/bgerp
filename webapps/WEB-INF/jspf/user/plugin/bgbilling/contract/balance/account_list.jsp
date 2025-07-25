<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="balance_form.jsp"%>

<table class="data hl">
	<tr>
		<td>Дата</td>
		<td width="100%">Услуга</td>
		<td>Сумма</td>
	</tr>
	<c:forEach var="account" items="${frd.list}">
		<tr>
			<td nowrap="nowrap">${account.month}</td>
			<td>${account.title}</td>
			<td nowrap="nowrap">${account.sum}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="subList" value="${frd.subList}"/>
<c:if test="${not empty subList}">
	<table class="data hl">
		<tr>
			<td>Договор</td>
			<td>Дата</td>
			<td width="100%">Услуга</td>
			<td>Сумма</td>
		</tr>
		<c:forEach var="account" items="${subList}">
			<tr>
				<td nowrap="nowrap">${account.contract}</td>
				<td nowrap="nowrap">${account.month}</td>
				<td>${account.title}</td>
				<td nowrap="nowrap">${account.sum}</td>
			</tr>
		</c:forEach>
	</table>
</c:if>