<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${values.contains(node.id)}">
	<c:set var="selected" value=" checked='true' "/>
</c:if>

<c:if test="${not(node.title.startsWith('@'))}">
	<ul>
		<li class="select_node ${selected}">
			<c:set var="nodeValue" value="${node.id}"/>
			<c:set var="nodeName" value="${paramName}"/>
			<c:if test="${not empty node.children}">
				<c:set var="nodeValue" value=""/>
				<c:set var="nodeName" value=""/>
			</c:if>

			<label class="ml05">
				<c:choose>
					<c:when test="${empty node.children}">
						<input name="${nodeName}" type="checkbox" value="${nodeValue}" ${selected}/>
					</c:when>
					<c:otherwise>
						&nbsp;
					</c:otherwise>
				</c:choose>
				${node.title}
			</label>

			<c:remove var="selected"/>

			<c:if test="${not empty node.children}">
				<c:forEach var="child" items="${node.children}">
					<c:set var="node" value="${child}" scope="request"/>
					<c:set var="parent" value="${node}" scope="request"/>
					<jsp:include page="tree_item.jsp"/>
				</c:forEach>
			</c:if>
		</li>
	</ul>
 </c:if>
