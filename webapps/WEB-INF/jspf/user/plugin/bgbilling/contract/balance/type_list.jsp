<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="padding-left: ${10 * level}px; width: inherit;">
	<c:choose>
		<c:when test="${not empty node.children}">
			<span id="${node.id}_title" onclick="$(this.parentNode).find( '#' + ${node.id} + '_childs' ).toggle();">
				<img border="0" src="/img/folder.gif" alt="Node"/>&nbsp;${node.title}
			</span>
			<div id="${node.id}_childs" style="display: none;">
				<c:forEach var="child" items="${node.children}">
					<c:set var="level" value="${level + 1}" scope="request"/>
					<c:set var="node" value="${child}" scope="request"/>
					<jsp:include page="type_list.jsp"/>
					<c:set var="level" value="${level - 1}" scope="request"/>
				</c:forEach>
			</div>
		</c:when>
		<c:otherwise>
			<span id="${node.id}_title" onclick="bgbilling_typeListNodeSelected($(this),${node.id});">
				<img border="0" style="cursor: pointer;" src="/img/page.png" alt="Type"/>&nbsp;${node.title}
			</span>
		</c:otherwise>
	</c:choose>
</div>