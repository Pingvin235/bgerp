<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="${form.httpRequestURI}" styleClass="in-mr1" style="display: inline-block;">
	<input type="hidden" name="method" value="list"/>

	<c:url var="url" value="${form.httpRequestURI}">
		<c:param name="method" value="get"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<ui:button type="add" onclick="$$.ajax.loadContent('${url}', this)"/>

	<ui:input-text name="filter" value="${form.param['filter']}" size="20" placeholder="${l.l('Filter')}"
		title="${l.l('Фильтр по содержимому конфигурации')}"
		onSelect="$$.ajax.loadContent(this); return false;"/>
</html:form>

<div style="display: inline-block;" class="ml2">
	<%@ include file="../app/app_restart.jsp"%>
</div>

<table class="data mt1 hl">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="50">${l.l('Активный')}</td>
		<td width="50%">${l.l('Title')}</td>
		<td width="50%">${l.l('Включенные плагины')}</td>
		<td>&nbsp;</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<c:set var="item" scope="request" value="${item}"/>
		<jsp:include page="config_item.jsp"/>
	</c:forEach>
</table>

<shell:title text="${l.l('Configuration')}"/>
<shell:state help="kernel/setup.html#config"/>
