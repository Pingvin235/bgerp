<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="showCode" value="$$.ajax.load($('#${uiid}'), $('#${uiid}').parent());"/>

<c:url var="getUrl" value="/user/plugin/dispatch/dispatch.do">
	<c:param name="action" value="dispatchGet"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<html:form action="/user/plugin/dispatch/dispatch" styleClass="in-mr1 in-mb1" styleId="${uiid}" style="vertical-align: middle;">
	<input type="hidden" name="action" value="dispatchList"/>

	<ui:button type="add" onclick="$$.ajax.load('${getUrl}', $('#${uiid}').parent())"/>

	<ui:page-control/>
</html:form>

<table class="data">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td>${l.l('Title')}</td>
		<td>${l.l('Комментарий')}</td>
		<td>${l.l('Subscribers')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<c:url var="editUrl" value="${getUrl}">
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteUrl" value="/user/plugin/dispatch/dispatch.do">
				<c:param name="action" value="dispatchDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>

			<td nowrap="nowrap">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $('#${uiid}').parent())"/>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}').done(() => { ${showCode} })"/>
			</td>
			<td>${item.id}</td>
			<td>${item.title}</td>
			<td>${item.comment}</td>
			<td>${item.accountCount}</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="${l.l('Рассылки')}"/>