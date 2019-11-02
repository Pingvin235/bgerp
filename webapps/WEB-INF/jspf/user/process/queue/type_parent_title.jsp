<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>


<c:if test="${curType.parentId > 0}">
	<c:set var="curTypeId" value="${curType.id}"/>
	<c:set var="curType" value="${ctxProcessTypeMap[curType.parentId]}" scope="request"/>
	<jsp:include page="type_parent_title.jsp"/>		
	<c:set var="curType" value="${ctxProcessTypeMap[curTypeId]}" scope="request"/>	
</c:if>

<c:set var="typeTitle" scope="request">
	${typeTitle}-${curType.title}
</c:set>					
