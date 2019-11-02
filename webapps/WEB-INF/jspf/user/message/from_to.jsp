<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="from_to">${fn:escapeXml( item.from )} -&gt; ${fn:escapeXml( item.to )}</c:set>
<c:set var="from_to_full" value="${from_to}"/>
<c:if test="${from_to.length() gt 60}">
	<c:set var="from_to">${fn:substring(from_to, 0, 60)}...</c:set>
</c:if>
<td title="${from_to_full}">${from_to}</td>