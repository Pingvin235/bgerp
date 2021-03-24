<%@ tag pageEncoding="UTF-8" description="Iface filter"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="type" description="Comma separated iface names"%>

<c:set var="types" value="${u.toSet(type)}"/>

<c:if test="${types.contains(ctxIface)}">
	<jsp:doBody/>
</c:if>
