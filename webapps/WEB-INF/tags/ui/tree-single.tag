<%@ tag body-content="empty" pageEncoding="UTF-8" description="Tree with single item selection element"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="Container ID, if empty - will be generated." %>
<%@ attribute name="rootNode" description="Tree root node" type="org.bgerp.model.base.tree.TreeItem" required="true" %>
<%@ attribute name="name" description="hidden input's name" required="true" %>
<%@ attribute name="nameTitle" description="hidden input's name for storing node title" %>
<%@ attribute name="selectableFolder" description="selectable folder nodes (enabled by default)" %>
<%@ attribute name="value" description="current hidden's value" type="java.lang.Object" %>
<%@ attribute name="styleClass" description="Additional CSS classes for container" %>
<%@ attribute name="style" description="CSS styles for container" %>

<c:if test="${empty id}">
	<c:set var="id" value="${u:uiid()}"/>
</c:if>

<c:set var="selectableFolder" value="${u.parseBoolean(selectableFolder, true)}"/>

<div id="${id}" class="tree-single ${styleClass}" style="${style}">
	<input type="hidden" name="${name}" value="${value}"/>
	<c:if test="${not empty nameTitle}">
		<input type="hidden" name="${nameTitle}" value="${rootNode.getById(value).title}"/>
	</c:if>

	<ui:tree-single-item node="${rootNode}" selectableFolder="${selectableFolder}" level="0"/>
</div>

<script>
	$(function () {
		$$.ui.tree.single.init("${id}", "${value}", "${name}", "${nameTitle}");
		if (!"${value}")
			$$.ui.tree.single.openRoot("${id}");
	})
</script>
