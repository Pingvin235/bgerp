<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${processType.properties.configMap['document:processShowDocuments'] eq 1}">
	<c:set var="plugin" value="${ctxPluginManager.pluginMap['document']}" />
	<c:if test="${not empty plugin}">
		<c:url var="url" value="plugin/document/document.do">
			<c:param name="scope" value="process" />
			<c:param name="objectType" value="process" />
			<c:param name="objectTitle" value="${process.id}" />
			<c:param name="objectId" value="${process.id}" />
			<c:param name="id" value="${process.id}" />
		</c:url>
	
	$tabs.tabs( 'add', "${url}", "Документы" );
	</c:if>
</c:if>