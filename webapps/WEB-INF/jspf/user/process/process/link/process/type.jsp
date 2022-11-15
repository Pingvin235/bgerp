<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${type eq 'processLink'}"><span title="Link">[L]</span></c:when>
	<c:when test="${type eq 'processDepend'}"><span title="Depend">[D]</span></c:when>
	<c:when test="${type eq 'processMade'}"><span title="Made">[M]</span></c:when>
</c:choose>