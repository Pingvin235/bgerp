<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>${l.l('Выберите тип')}</h2>

<c:set var="uiid" value="${u:uiid()}"/>

<div style="background-color: #ffffff; cursor: pointer;" id="${uiid}">
	<input type="hidden" name="typeId"/>

	<c:forEach var="node" items="${typeTreeRoot.children}">
		<c:set var="node" value="${node}" scope="request"/>
		<c:set var="level" value="0" scope="request"/>

		<jsp:include page="process_type_tree_item.jsp"/>
	</c:forEach>
</div>

<c:remove var="afterSelectCommand"/>

<script>
$(function () {
	$$.process.type.select('${uiid}');
})
</script>