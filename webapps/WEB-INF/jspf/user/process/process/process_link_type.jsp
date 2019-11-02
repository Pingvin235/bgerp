<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${type eq 'processLink'}"><span title="Ссылается">[С]</span></c:when>
	<c:when test="${type eq 'processDepend'}"><span title="Зависит">[З]</span></c:when>
	<c:when test="${type eq 'processMade'}"><span title="Породил">[П]</span></c:when>
</c:choose>