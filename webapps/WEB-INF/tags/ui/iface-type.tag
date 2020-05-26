<%@ tag pageEncoding="UTF-8" description="Iface filter"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:set var="uri"><%=org.bgerp.servlet.filter.OpenFilter.getRequestURI(request)%></u:set>
<u:set var="user"><%=ru.bgcrm.servlet.filter.AuthFilter.getUser(request)%></u:set>

<c:choose>
	<c:when test="${not empty uri}">open</c:when>
	<c:when test="${not empty user}">user</c:when>
	<c:otherwise>undef</c:otherwise>
</c:choose>