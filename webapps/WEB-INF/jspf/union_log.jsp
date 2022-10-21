<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="id" value="${form.param.id}"/>
<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="paramLinkId" value="${u:uiid()}"></c:set>

<html:form action="/user/process.do"  styleClass="center1020" styleId="${paramLinkId}">
	<input type="hidden" name="action" value="unionLog"/>
	<input type="hidden" name="id" value="${form.id}"/>
	<html:hidden property="returnUrl"/>

	<ui:button type="close" onclick="$$.ajax.load('${form.returnUrl}', $('#${paramLinkId}').parent())"/>

	<%-- не выносится в title область, т.к. лог может быть открыт у зависимого процесса --%>
	<h1 style="margin: 0; display: inline-block;" class="pl2">${l.l('Лог изменений')}</h1>

	<ui:page-control/>

	<table class="data mt1">
		<tr ${hideTr}>
			<td>${l.l('Дата')}</td>
			<td>${l.l('User')}</td>
			<td width="100%">${l.l('Значение')}</td>
		</tr>
		<c:forEach var="logItem" items="${form.response.data.list}">
			<tr>
				<td nowrap="nowrap">${logItem.getDateFormatted()}</td>
				<td nowrap="nowrap">${ctxUserMap[logItem.userId].title}</td>
				<td width="100%">${logItem.text}</td>
			</tr>
		</c:forEach>
	</table>
</html:form>



