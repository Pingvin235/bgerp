<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="title">
	<c:choose>
		<c:when test="${not empty filter.title}">${filter.title}</c:when>
		<c:otherwise>Исполнители</c:otherwise>
	</c:choose>
</c:set>

<%-- TODO: Use tag as in webapps\WEB-INF\jspf\admin\plugin\log\action.jsp --%>
<c:set var="code">
	<u:sc>
		<c:set var="id" value="${executorListId}"/>
		<c:set var="prefixText" value="${empty filter.title ? 'Исполнители:' : filter.title.concat(':')}"/>
		<%-- список сразу выстраивается фильтром групп --%>
		<c:set var="showFilter" value="1"/>
		<c:set var="widthTextValue" value="150px"/>
		<%@ include file="/WEB-INF/jspf/combo_check.jsp"%>
	</u:sc>
</c:set>

<%@ include file="item.jsp"%>