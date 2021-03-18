<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="subList" value="${form.response.data.subList}"/>
<c:if test="${not empty subList}">
	<table class="data mt1" width="100%" id="${uiid}">
		<tr>
			<td>${l.l('Договор')}</td>
			<td>${l.l('Дата')}</td>
			<td>${l.l('Сумма')}</td>
			<td nowrap="nowrap">${columnTitle}</td>
			<td width="100%">${l.l('Комментарий')}</td>
			<td nowrap="nowrap">${l.l('Время изменения')}</td>
			<td>${l.l('Пользователь')}</td>
		</tr>
		<c:forEach var="item" items="${form.response.data.subList}">
			<tr>
				<td>${item.contract}</td>
				<td>${u:formatDate( item.date, 'ymd' )}</td>
				<td>${item.sum}</td>
				<td>${item.type}</td>
				<td>${item.comment}</td>
				<td nowrap="nowrap">${u:formatDate( item.date, 'ymdhms' )}</td>
				<td nowrap="nowrap">${item.user}</td>
			</tr>
		</c:forEach>
	</table>
</c:if>