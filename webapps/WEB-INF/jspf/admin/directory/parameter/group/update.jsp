<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<c:set var="group" value="${form.response.data.group}"/>

<html:form action="/admin/directory" styleClass="center500">
	<input type="hidden" name="action" value="parameterGroupUpdate"/>
	<html:hidden property="directoryId"/>

	<h2>ID</h2>
	<input type="text" name="id" value="${group.id}" disabled="disabled" style="width: 100%;"/>

	<h2>${l.l('Название')}</h2>
	<input type="text" name="title" style="width: 100%" value="${group.title}"/>

	<h2>${l.l('Параметры')}</h2>
	<u:sc>
		<c:set var="list" value="${parameterList}" />
		<c:set var="hiddenName" value="param"/>
		<c:set var="values" value="${group.parameterIds}" />
		<c:set var="style" value="width: 100%;"/>
		<%@ include file="/WEB-INF/jspf/select_mult.jsp"%>
	</u:sc>

	<ui:form-ok-cancel styleClass="mt1"/>
</html:form>

<c:set var="state" value="${l.l('Редактор')}"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>