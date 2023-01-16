<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${fn:startsWith( linkedObjectType, 'contract:' )}">
		<c:set var="billingId" value="${su.substringAfter( linkedObjectType, ':')}"/>
		Договор:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}
	</c:when>
</c:choose>
