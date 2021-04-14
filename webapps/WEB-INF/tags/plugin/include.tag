<%@ tag body-content="empty" pageEncoding="UTF-8" description="Add endpoints from plugins"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="endpoint" description="Endpoint"%>

<%@tag import="ru.bgerp.l10n.Localization"%>
<%@tag import="ru.bgcrm.plugin.Plugin"%>

<c:forEach items="${ctxPluginManager.pluginList}" var="plugin">
	<c:set var="plugin" value="${plugin}" scope="request"/>
	
	<%
		Plugin p = (Plugin) request.getAttribute("plugin");
		request.setAttribute("l", p.getLocalizer(Localization.getLang(request)));
	%>

	<c:forEach items="${plugin.getEndpoints(endpoint)}" var="page">
		<jsp:include page="${page}"/>
	</c:forEach>
</c:forEach>