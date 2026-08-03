<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="allowedStatusIds" value="${process.allowedToChangeStatusIds}"/>

<div class="kanban-card" bg-process-id="${process.id}" bg-allowed="${u.toString(allowedStatusIds)}"
	draggable="${canDrag and allowedStatusIds.size() gt 1}"
	style="border-left-color: ${process.priorityColor}">
	<div class="kanban-card-title">
		<c:choose>
			<c:when test="${previewEnabled}">
				<a href="#" onclick="$$.kanban.openPreview(${process.id}); return false;">${process.id}</a>
			</c:when>
			<c:otherwise>
				<ui:process-link id="${process.id}"/>
			</c:otherwise>
		</c:choose>
		&nbsp;${process.description}
	</div>
	<c:if test="${not empty process.executorIds}">
		<div class="kanban-card-executors">
			<c:forEach var="uid" items="${process.executorIds}"><ui:user-link id="${uid}"/> </c:forEach>
		</div>
	</c:if>
</div>
