<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="linkList" value="${frd.list}"/>
<c:set var="whatShow" value="${form.param.whatShow}"/>

<c:remove var="form"/>

<c:forEach var="link" items="${linkList}">
	<c:set var="billingId" value="${su.substringAfter( link.linkObjectType, ':' )}"/>
	<h1>${link.linkObjectTitle} [ ${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title} ]</h1>

	<c:url var="url" value="/user/plugin/bgbilling/contract.do">
		<c:param name="method" value="contractInfo"/>
		<c:param name="billingId" value="${billingId}"/>
		<c:param name="contractId" value="${link.linkObjectId}"/>
		<c:param name="whatShow" value="${whatShow}"/>
	</c:url>
	<c:import url="${url}"/>
</c:forEach>
