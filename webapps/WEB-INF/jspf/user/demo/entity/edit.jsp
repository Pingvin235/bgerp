<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="entity" value="${frd.entity}"/>

<html:form action="${form.requestURI}" styleClass="box">
	<html:hidden property="method" value="entityUpdate"/>

	<h1>Entity Editor</h1>

	<div style="display: flex; flex-wrap: wrap;">
		<div style="flex-grow: 1;">
			<h2>ID</h2>
			<input type="text" name="id" disabled="true" value="${entity.id}" class="w100p"/>
		</div>
		<div style="flex-grow: 1;" class="pl1">
			<h2>Title</h2>
			<input type="text" name="title" value="${entity.title}" class="w100p"/>
		</div>
		<%-- second line --%>
		<c:set var="taUiid" value="${u:uiid()}"/>
		<div style="flex-basis: 100%;">
			<h2>Configuration</h2>
			<textarea name="config" id="${taUiid}" rows="20" class="w100p">${entity.config}</textarea>
		</div>
		<script>
			$$.ui.codeMirror('${taUiid}');
		</script>
	</div>

	<div class="mt1">
		<ui:form-ok-cancel/>
	</div>
</html:form>
