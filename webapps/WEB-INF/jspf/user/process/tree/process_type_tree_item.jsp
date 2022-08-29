<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="padding-left: ${level eq 0 ? 0 : 15}px; width: 100%;">
	<c:choose>
		<c:when test="${not empty node.children}">
			<span id="${node.id}_title" class="treeNode" onclick="openProcessTypeTreeNode( this, ${node.id} );">
				<img border="0" src="/img/folder.gif" alt="Node"/>&nbsp;${node.title}
			</span>
			<div id="${node.id}_childs" style="display: none;">
				<c:forEach var="child" items="${node.children}">
					<c:set var="level" value="${level + 1}" scope="request"/>
					<c:set var="node" value="${child}" scope="request"/>
					<jsp:include page="process_type_tree_item.jsp"/>
					<c:set var="level" value="${level - 1}" scope="request"/>
				</c:forEach>
			</div>
		</c:when>
		<c:otherwise>
			<c:url var="requestUrl" value="/user/process.do">
				<c:param name="action" value="processRequest"/>
				<c:param name="objectId" value="${form.id}"/>
				<c:param name="typeId" value="${node.id}"/>
				<c:param name="billingId" value="${fn:split(form.param.objectType,':')[1]}"/>
			</c:url>

			<c:if test="${not empty processTypeTree}">
				<c:set var="afterSelectCommand" value="openUrlTo('${requestUrl}', $('#${processTypeTree}').find('div[id=additionalParamsSelect]'));"/>
			</c:if>

			<span id="${node.id}_title" onclick="processTypeTreeNodeSelected( this, ${node.id}); ${afterSelectCommand}">
				<img border="0" style="cursor: pointer;" src="/img/page.png" alt="Type"/>&nbsp;${node.title}
			</span>
		</c:otherwise>
	</c:choose>
</div>