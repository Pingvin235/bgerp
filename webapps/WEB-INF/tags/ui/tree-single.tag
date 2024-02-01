<%@ tag body-content="empty" pageEncoding="UTF-8" description="Tree with single item selection element"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="Container ID, if empty - will be generated." type="java.lang.String"%>
<%@ attribute name="rootNode" description="Tree root node" required="true" type="org.bgerp.model.base.tree.TreeItem"%>
<%@ attribute name="selectableFolder" description="Selectable folder nodes (enabled by default)"%>
<%@ attribute name="hiddenName" description="Input type hidden parameter name" required="true" type="java.lang.String"%>
<%@ attribute name="hiddenNameTitle" description="Optional hidden parameter name for storing node title" type="java.lang.String"%>
<%@ attribute name="value" description="Current hidden' value" type="java.lang.Object"%>
<%@ attribute name="styleClass" description="Additional CSS classes for container" type="java.lang.String"%>
<%@ attribute name="style" description="CSS styles for container" type="java.lang.String"%>

<c:if test="${empty id}">
	<c:set var="id" value="${u:uiid()}"/>
</c:if>

<c:set var="selectableFolder" value="${u.parseBoolean(selectableFolder, true)}"/>

<div id="${id}" class="tree-single ${styleClass}" style="${style}">
	<input type="hidden" name="${hiddenName}" value="${value}"/>
	<c:if test="${not empty hiddenNameTitle}">
		<input type="hidden" name="${hiddenNameTitle}" value="${rootNode.getById(value).title}"/>
	</c:if>

	<ui:tree-single-item node="${rootNode}" selectableFolder="${selectableFolder}" level="0"/>
</div>

<script>
	$(function () {
		$$.ui.tree.single.init("${id}", "${value}", "${hiddenName}", "${hiddenNameTitle}");
		if (!"${value}")
			$$.ui.tree.single.openRoot("${id}");
	})
</script>
