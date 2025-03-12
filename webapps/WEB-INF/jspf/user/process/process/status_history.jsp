<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/process" styleId="${uiid}" styleClass="center1020">
	<html:hidden property="id" />
	<input type="hidden" name="method" value="processStatusHistory" />

	<ui:button type="close" styleClass="mb1" onclick="$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent());"/>

	<table class="data">
		<tr>
			<td>${l.l('Status')}</td>
			<td>${l.l('Comment')}</td>
			<td>${l.l('Time')}</td>
			<td>${l.l('User')}</td>
		</tr>
		<c:forEach var="item" items="${frd.list}">
			<tr>
				<td>${item.statusTitle}</td>
				<td>${item.comment}</td>
				<td>${tu.format( item.date, 'ymdhms' )}</td>
				<td>${item.userTitle}</td>
			</tr>
		</c:forEach>
	</table>

</html:form>

<shell:state text="${l.l('История статусов')}"/>