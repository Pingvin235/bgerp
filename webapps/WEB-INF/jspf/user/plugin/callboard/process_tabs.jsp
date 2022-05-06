<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="timeSetConfig" value="${processType.properties.configMap.getConfig('org.bgerp.plugin.pln.callboard.model.config.ProcessTimeSetConfig')}"/>
<c:if test="${not empty timeSetConfig.callboard}">
	<c:url var="url" value="/user/plugin/callboard/work.do">
		<c:param name="action" value="processTime"/>
		<c:param name="processId" value="${process.id}"/>
	</c:url>

	$tabs.tabs("add", "${url}", "Уст. времени");
</c:if>