<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Выберите исполнителей')}</h1>

<html:form action="${form.requestURI}">
	<html:hidden property="id"/>
	<input type="hidden" name="method" value="processExecutorsUpdate"/>

	<c:forEach var="item" items="${frd.groupsWithRoles}">
		<input type="hidden" name="group" value="${item.first.id}"/>
		<h2>${item.first.title}</h2>

		<ui:select-mult name="executor" list="${item.second[0]}" values="${item.second[1]}" style="width: 100%;"/>
	</c:forEach>

	<div class="mt1">
		<%@ include file="editor_save_cancel.jsp"%>
	</div>
</html:form>