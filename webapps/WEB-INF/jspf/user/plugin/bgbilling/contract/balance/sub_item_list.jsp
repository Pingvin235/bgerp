<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="subList" value="${frd.subList}"/>
<c:if test="${not empty subList}">
	<table class="data mt1 hl" id="${uiid}">
		<tr>
			<td>Договор</td>
			<td>Дата</td>
			<td>Сумма</td>
			<td nowrap="nowrap">${columnTitle}</td>
			<td width="100%">Комментарий</td>
			<td nowrap="nowrap">Время изменения</td>
			<td>Пользователь</td>
		</tr>
		<c:forEach var="item" items="${frd.subList}">
			<tr>
				<td>${item.contract}</td>
				<td>${tu.format( item.date, 'ymd' )}</td>
				<td>${item.sum}</td>
				<td>${item.type}</td>
				<td>${item.comment}</td>
				<td nowrap="nowrap">${tu.format( item.date, 'ymdhms' )}</td>
				<td nowrap="nowrap">${item.user}</td>
			</tr>
		</c:forEach>
	</table>
</c:if>