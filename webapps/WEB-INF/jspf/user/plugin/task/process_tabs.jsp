<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${processType.properties.configMap['task:processShowTasks'] eq 1}">
	<c:set var="plugin" value="${ctxPluginManager.pluginMap['task']}" />
	<c:if test="${not empty plugin}">
		<c:url var="url" value="plugin/task/task.do">
			<c:param name="method" value="list" />
			<c:param name="processId" value="${process.id}" />
		</c:url>
		$tabs.tabs('add', "${url}", "Задачи");
	</c:if>
</c:if>