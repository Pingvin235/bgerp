<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${fn:startsWith( linkedObjectType, 'contract:' )}">
		<c:set var="billingId" value="${fn:substringAfter( linkedObjectType, ':')}"/>
		${l.l('Договор')}:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}
	</c:when>
	<c:when test="${linkedObjectType eq 'bgbilling-commonContract'}">Единый договор</c:when>
</c:choose>	
