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

<td colspan="${colspan}" class="${filters} bgcolor-${color}" bg-id="${item.processId}" bg-parent-id="${parentId}" bg-type-id="${item.process.typeId}" draggable="${empty item.children}">
	<c:set var="content">
		<c:choose>
			<c:when test="${not empty open}"><a id="${item.process.id}" href="#${item.process.id}">${item.process.id}</a>&nbsp;</c:when>
			<c:otherwise><ui:process-link id="${item.process.id}"/>&nbsp;</c:otherwise>
		</c:choose>
		${board.config.getCellContent(item)}
	</c:set>
	
	<c:choose>
		<c:when test="${not empty item.children}"><b>${content}</b></c:when>
		<c:otherwise>${content}</c:otherwise>
	</c:choose>
</td>