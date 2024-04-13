<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="title">
	<c:choose>
		<c:when test="${not empty filter.title}">${filter.title}</c:when>
		<c:otherwise>${l.l('Executors')}</c:otherwise>
	</c:choose>
</c:set>

<c:set var="code">
	<ui:combo-check id="${executorListId}"
		prefixText="${empty filter.title ? l.l('Executors') : filter.title}:"
		showFilter="1"
		widthTextValue="10em"/>
</c:set>

<%@ include file="item.jsp"%>