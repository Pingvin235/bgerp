<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/user/process">
	<html:hidden property="typeId"/>
	<html:hidden property="id"/>
	<input type="hidden" name="method" value="processTypeUpdate"/>

	<h1>${l.l('Выберите тип')}</h1>
	<c:set var="uiid" value="${u:uiid()}"/>
	<div id="${uiid}" style="background-color: #ffffff; cursor: pointer;" class="p05">
		<c:forEach var="node" items="${typeTreeRoot.children}">
			<c:set var="node" value="${node}" scope="request"/>
			<c:set var="level" value="0" scope="request"/>
			<jsp:include page="../tree/process_type_tree_item.jsp"/>
		</c:forEach>
	</div>
	<%@ include file="editor_save_cancel.jsp"%>
	<script>
		$(function () {
			$$.process.type.selectEdit('${uiid}', '${form.param.typeId}');
		})
	</script>
</html:form>