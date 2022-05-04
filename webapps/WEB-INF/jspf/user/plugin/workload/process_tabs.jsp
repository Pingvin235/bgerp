<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="plugin" value="${ctxPluginManager.pluginMap['workload']}" />
<c:if test="${not empty plugin}">
	<c:set var="config" value="${processType.properties.configMap.getConfig('org.bgerp.plugin.pln.workload.model.GroupLoadConfig')}"/>

	<c:if test="${config.enabled}">
		<c:url var="url" value="plugin/workload/groupload.do">
			<c:param name="action" value="show" />
			<c:param name="processId" value="${process.id}" />
			<c:param name="processTypeId" value="${process.typeId}" />
		</c:url>
		$tabs.tabs('add', "${url}", "Загрузка");
	</c:if>
</c:if>