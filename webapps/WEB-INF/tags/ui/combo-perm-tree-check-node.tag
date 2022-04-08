<%@ tag body-content="empty" pageEncoding="UTF-8" description="Drop down list with multiselect"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="node" required="true" type="ru.bgcrm.model.user.PermissionNode" description="Permission tree node"%>
<%@ attribute name="values" type="java.util.Collection" description="current values"%>

<c:if test="${not node.allowAll}">
	<c:set var="selected">
		<c:if test="${not empty node.action and values.contains(node.action)}">checked='true'</c:if>
	</c:set>

	<li class="select_node ${selected}">
		<label><input name="perm" type="checkbox" ${selected} value="${node.action}"/>${node.title}</label>

		<c:if test="${not empty node.children}">
			<ul>
				<c:forEach var="child" items="${node.children}">
					<ui:combo-perm-tree-check-node node="${child}" values="${values}"/>
				</c:forEach>
			</ul>
		</c:if>
	</li>
</c:if>