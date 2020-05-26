<%@ tag pageEncoding="UTF-8" description="Iface filter"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="type" description="comma seprated iface names"%>

<c:set var="types" value="${u.toSet(type)}"/>
<c:set var="type"><ui:iface-type/></c:set>

<c:if test="${types.contains(type)}">
	<jsp:doBody/>
</c:if>
