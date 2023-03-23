<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<li>
	<label class="ml05">
		<input type="checkbox" name="type" value="${node.id}" ${u:checkedFromCollection( processTypeIds, node.id )}/> ${node.title} (${node.id})
	</label>

	<c:if test="${not empty node.children}">
		<ul>
			<c:forEach var="child" items="${node.children}">
				<c:set var="node" value="${child}" scope="request"/>
				<jsp:include page="process_type_check_tree_item.jsp"/>
			</c:forEach>
		</ul>
	</c:if>
</li>
