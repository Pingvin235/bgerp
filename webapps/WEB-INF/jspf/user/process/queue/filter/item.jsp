<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${not empty id}">
		<c:set var="uiid" value="${id}"/>
	</c:when>
	<c:otherwise>
		<c:set var="uiid" value="${u:uiid()}"/>
	</c:otherwise>
</c:choose>

<c:if test="${empty filter.show or filter.show}">
	<li>
		<input id="${uiid}" type="checkbox" ${u:checkedFromCollection(selectedFilters, filter.id)} value="${filter.id}"/>
		<span>${title}</span>
	</li>
</c:if>
<c:set var="filters" scope="request">
	${filters}
	<div id="${uiid}" style="display: none;" class="filter-item">
		${code}
	</div>
</c:set>

<%-- TODO: Remove later, seems to be not needed.
<script>
	$$.process.queue.filter.checkedIfInputValue('${uiid}');
</script>
--%>

<c:remove var="uiid"/>
