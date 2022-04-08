<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ul>
	<c:forEach var="child" items="${node.children}">
		<c:set var="node" value="${child}" scope="request" />
			<c:if test="${not node.allowAll}">
				<c:set var="configDialogId" value="${u:uiid()}"/>
				<c:set var="configParamId" value="${u:uiid()}"/>
				<c:set var="textareaId" value="${u:uiid()}"/>
				<c:set var="linkId" value="${u:uiid()}"/>
				<c:set var="permConf" value="${grantedPermission[node.action]}" />
				
				<c:if test="${grantedPermission.containsKey(node.action)}">
					<c:set var="selected" value=" checked='true' "/>
				</c:if>
		
				<li class="select_node ${selected}">
					<label>
						<input name="${paramName}" type="checkbox" ${selected} value="${node.action}"/>
						${node.title}
						<c:if test="${not empty node.action }">
							(${node.action})
						</c:if>
					</label>
					
					<c:set var="selected" value=""/>
					<c:set var="permConf" value=""/>
				
					<c:if test="${not empty node.children}">
						<jsp:include page="check_tree_item.jsp" />
					</c:if>
				</li>
			</c:if>
	</c:forEach>
</ul>