<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${not empty process.reference}">
		${process.reference}
	</c:when>
	<c:otherwise>
		${u.escapeXml(process.description)}
	</c:otherwise>
</c:choose>