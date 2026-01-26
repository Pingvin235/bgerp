<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${processType.properties.configMap['task:processShowTasks'] eq 1 and ctxUser.checkPerm('/user/plugin/task/process:null')}">
	<c:set var="plugin" value="${ctxPluginManager.pluginMap.task}" />
	<c:if test="${not empty plugin}">
		<c:url var="url" value="/user/plugin/task/process.do">
			<c:param name="id" value="${process.id}" />
		</c:url>
		$tabs.tabs('add', "${url}", "${l.l("Tasks")}");
	</c:if>
</c:if>