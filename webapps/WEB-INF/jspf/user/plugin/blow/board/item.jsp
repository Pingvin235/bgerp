<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="priority" value="${item.priority}"/>
<%@ include file="/WEB-INF/jspf/process_color.jsp"%>

<%-- empty bg-parent-id for groups to avoid undistributed group relation --%>
<c:set var="parentId" value="${item.parent.processId}"/>
<c:if test="${parentId eq 0 and not empty item.children}">
	<c:set var="parentId" value=""/>
</c:if>

<c:set var="filters">
	<c:forEach var="filterId" items="${item.filterIds}"> filter-${filterId} </c:forEach>
</c:set>

<c:set var="draggable">
	<ui:when type="user">${empty item.children}</ui:when>
</c:set>

<td colspan="${colspan}" class="${filters} bgcolor-${color}" bg-id="${item.processId}" bg-parent-id="${parentId}" bg-type-id="${item.process.typeId}" draggable="${draggable}">
	<c:set var="content">
		<ui:process-link id="${item.process.id}" process="${item.process}"/>&nbsp;
		${board.config.getCellContent(item)}
	</c:set>
	
	<c:choose>
		<c:when test="${not empty item.children}"><b>${content}</b></c:when>
		<c:otherwise>${content}</c:otherwise>
	</c:choose>
</td>