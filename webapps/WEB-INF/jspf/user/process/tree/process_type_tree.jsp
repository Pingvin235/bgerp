<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- afterSelectCommand - команда выполняется после клика по типу процесса --%>

<b>${l.l('Выберите тип')}:</b><br/>

<c:set var="uiid" value="${u:uiid()}"/>

<div style="background-color: #ffffff; cursor: pointer;" id="${uiid}">
	<input type="hidden" name="typeId"/>
	
	<c:forEach var="node" items="${typeTreeRoot.childs}">
		<c:set var="node" value="${node}" scope="request"/>
		<c:set var="level" value="0" scope="request"/>
		
		<jsp:include page="process_type_tree_item.jsp"/>
	</c:forEach>
</div>

<c:remove var="afterSelectCommand"/>

<script>
$(function() {
	$('#${uiid} span.treeNode').first().click();
})
</script>