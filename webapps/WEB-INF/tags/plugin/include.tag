<%@ tag body-content="empty" pageEncoding="UTF-8" description="Add endpoints from plugins"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="endpoint" description="Endpoint"%>

<%@tag import="ru.bgcrm.plugin.Plugin"%>

<c:forEach items="${ctxPluginManager.pluginList}" var="plugin">
	<c:set var="plugin" value="${plugin}" scope="request"/>
	<c:set var="endpoints" value="${plugin.getEndpoints(endpoint)}"/>

	<c:if test="${not empty endpoints}">
		<%
			Plugin p = (Plugin) request.getAttribute("plugin");
			request.setAttribute("l", p.getLocalizer(org.bgerp.l10n.Localization.getLang(request)));
		%>

		<c:forEach items="${endpoints}" var="page">
			<jsp:include page="${page}"/>
		</c:forEach>
	</c:if>
</c:forEach>