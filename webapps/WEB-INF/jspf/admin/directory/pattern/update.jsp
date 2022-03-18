<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<c:set var="pattern" value="${form.response.data.pattern}"/>

<html:form action="/admin/directory" styleClass="center500">
	<input type="hidden" name="action" value="patternTitleUpdate"/>
	<html:hidden property="directoryId"/>

	<h2>ID</h2>
	<input type="text" name="id" value="${pattern.id}" disabled="disabled" style="width: 100%;"/>

	<h2>${l.l('Название')}</h2>
	<input type="text" name="title" style="width: 100%" value="${pattern.title}"/>

	<h2>${l.l('Шаблон')}</h2>
	<input type="text" name="pattern" style="width: 100%" value="${fn:escapeXml( pattern.pattern )}"/>

	<ui:form-ok-cancel styleClass="mt1"/>
</html:form>

<c:set var="state" value="${l.l('Редактор')}"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
