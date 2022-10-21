<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="id" value="${form.param.id}"/>
<c:set var="log" value="${form.response.data.log}"/>
<c:set var="paramLinkId" value="${u:uiid()}"></c:set>
<c:set var="nextCommand" value="; $$.ajax.load(this.form, $('#${paramLinkId}').parent())"/>

<h1>${l.l('Лог изменений параметров')}</h1>

<html:form action="/user/parameter" style="width: 100%;" styleId="${paramLinkId}">
	<table style="width: 100%;" id="${paramLinkId}">
		<tr><td>
			<ui:button type="close" onclick="$$.ajax.load('${form.returnUrl}', $('#${paramLinkId}').parent())"/>
			<ui:page-control nextCommand="${nextCommand}"/>
		</td></tr>
	</table>

	<input type="hidden" name="action" value="parameterLog"/>
	<input type="hidden" name="id" value="${form.id}"/>
	<html:hidden property="objectType"/>
	<html:hidden property="returnUrl"/>
	<table id="${paramLinkId}" class="data mt05">
		<tr>
			<td>${l.l('Дата')}</td>
			<td>${l.l('User')}</td>
			<td>${l.l('Параметр')}</td>
			<td width="100%">${l.l('Значение')}</td>
		</tr>
		<c:forEach var="logItem" items="${log}">
			<tr>
				<td nowrap="nowrap">${logItem.getDateFormatted()}</td>
				<td nowrap="nowrap">${ctxUserMap[logItem.userId].title}</td>
				<td nowrap="nowrap">${ctxParameterMap[logItem.paramId].title}</td>
				<td width="100%">${logItem.text}</td>
			</tr>
		</c:forEach>
	</table>
	<ui:page-control nextCommand="${nextCommand}"/>
</html:form>


