<%@ tag body-content="empty" pageEncoding="UTF-8" description="Tree item"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="node" description="Tree node" required="true" type="ru.bgcrm.model.IdTitleTreeItem"%>
<%@ attribute name="level" description="Input type hidden parameter name" required="true" type="java.lang.String"%>
<%@ attribute name="selectableFolder" description="Selectable folder nodes" type="java.lang.Boolean"%>

<div class="item">
	<c:set var="children" value="${not empty node.children}"/>

	<div class="title" id="title-${node.id}">
		<c:set var="onclick">
			<c:if test="${children}">
				onclick="$$.ui.tree.single.expand(this.parentElement)"
			</c:if>
		</c:set>
		<i class="expander ${children ? 'folder' : ''} ti-angle-right" ${onclick}></i>

		<c:set var="onclick">
			<c:if test="${not children or selectableFolder}">
				onclick="$$.ui.tree.single.select(this.parentElement, ${node.id})"
			</c:if>
		</c:set>
		<span ${onclick}>
			<span class="icon">${node.icon}</span>
			<c:set var="style" value="${node.textStyle}"/>
			<c:set var="style"><c:if test="${not empty style}"> style="${style}"</c:if></c:set>
			<span class="text"${style}>${node.title}</span>
		</span>
	</div>

	<c:if test="${children}">
		<div class="children">
			<c:forEach items="${node.children}" var="child">
				<ui:tree-single-item node="${child}" level="${level + 1}" selectableFolder="${selectableFolder}"/>
			</c:forEach>
		</div>
	</c:if>
</div>

