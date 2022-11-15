<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/process" styleId="${uiid}" styleClass="center1020">
	<html:hidden property="id" />
	<input type="hidden" name="action" value="processStatusHistory" />

	<button class="btn-white mb1" type="button" onclick="openUrlToParent( '${form.returnUrl}', $('#${uiid}') );">${l.l('Close')}</button>

	<table style="width: 100%;" class="data">
		<tr>
			<td>${l.l('Status')}</td>
			<td>${l.l('Комментарий')}</td>
			<td>${l.l('Время')}</td>
			<td>${l.l('User')}</td>
		</tr>
		<c:forEach var="item" items="${form.response.data.list}">
			<tr>
				<td>${item.statusTitle}</td>
				<td>${item.comment}</td>
				<td>${tu.format( item.date, 'ymdhms' )}</td>
				<td>${item.userTitle}</td>
			</tr>
		</c:forEach>
	</table>

</html:form>

<c:set var="state" value="${l.l('История статусов')}"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>