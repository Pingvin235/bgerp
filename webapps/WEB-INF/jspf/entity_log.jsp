<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="id" value="${form.param.id}"/>
<c:set var="log" value="${frd.log}"/>

<c:set var="parameterGroup" value="${frd.parameterGroup}"/>

<c:set var="paramLinkId" value="${u:uiid()}"></c:set>

<div class="center1020" id="${paramLinkId}">
	<ui:button styleClass="mb1" type="close" onclick="$$.ajax.load('${form.returnUrl}', $('#${paramLinkId}').parent())"/>
	<table class="data">
		<%-- TODO: Check, is hideTr somewhere used.  --%>
		<tr ${hideTr}>
			<td>${l.l('Date')}</td>
			<td>${l.l('User')}</td>
			<td width="100%">${l.l('Текст')}</td>
		</tr>
		<c:forEach var="logItem" items="${log}">
			<tr>
				<td nowrap="nowrap">${logItem.getDateFormatted()}</td>
				<td nowrap="nowrap">${ctxUserMap[logItem.userId].title}</td>
				<td width="100%">${logItem.text}</td>
			</tr>
		</c:forEach>
	</table>

	<shell:state text="${l.l('Лог изменений')}"/>
</div>




