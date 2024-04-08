<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="showCode" value="$$.ajax.loadContent($('#${uiid}'));"/>

<c:url var="getUrl" value="/user/plugin/dispatch/dispatch.do">
	<c:param name="action" value="messageGet"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<html:form action="/user/plugin/dispatch/dispatch" styleClass="in-mr1 in-mb1" styleId="${uiid}" style="vertical-align: middle;">
	<button type="button" class="btn-green" onclick="$$.ajax.loadContent('${getUrl}');">+</button>

	<input type="hidden" name="action" value="messageList"/>

	<button class="btn-grey" type="button" onclick="${showCode}">${l.l('Вывести')}</button>

	<ui:page-control nextCommand="${nextCommand}" />
</html:form>

<table class="data">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td  width="50%">${l.l('Title')}</td>
		<td width="50%">${l.l('Рассылки')}</td>
		<td nowrap="nowrap">${l.l('Creation time')}</td>
		<td>${l.l('Готово')}</td>
		<td nowrap="nowrap">${l.l('Время отправки')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
		 	<c:url var="editUrl" value="${getUrl}">
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/user/plugin/dispatch/dispatch.do">
				<c:param name="action" value="messageDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="${showCode}"/>

			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
			<td>${item.id}</td>
			<td>${item.title}</td>
			<td>${u.getObjectTitles( dispatchList, item.dispatchIds )}</td>
			<td nowrap="nowrap">${tu.format( item.createTime, 'ymdhms' )}</td>
			<td style="background-color: ${item.ready ? 'lightgreen' : ''};">${item.ready ? 'Да' : 'Нет'}</td>
			<td nowrap="nowrap">${tu.format( item.sentTime, 'ymdhms' )}</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="${l.l('Сообщения рассылок')}"/>
<shell:state/>