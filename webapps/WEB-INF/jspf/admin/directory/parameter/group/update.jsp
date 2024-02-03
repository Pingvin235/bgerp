<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<c:set var="group" value="${frd.group}"/>

<html:form action="/admin/directory" styleClass="center500">
	<input type="hidden" name="action" value="parameterGroupUpdate"/>
	<html:hidden property="directoryId"/>

	<h2>ID</h2>
	<input type="text" name="id" value="${group.id}" disabled="disabled" style="width: 100%;"/>

	<h2>${l.l('Название')}</h2>
	<input type="text" name="title" style="width: 100%" value="${group.title}"/>

	<h2>${l.l('Parameters')}</h2>
	<ui:select-mult hiddenName="param" list="${parameterList}" values="${group.parameterIds}" style="width: 100%;"/>

	<ui:form-ok-cancel styleClass="mt1"/>
</html:form>

<shell:state text="${l.l('Редактор')}"/>