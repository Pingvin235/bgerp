<%@ tag body-content="empty" pageEncoding="UTF-8" description="Add endpoints from plugins"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="endpoint" description="Endpoint"%>

<%@tag import="ru.bgerp.l10n.Localization"%>
<%@tag import="ru.bgcrm.plugin.Plugin"%>
<%@tag import="ru.bgerp.l10n.Localizer"%>

<c:forEach items="${ctxPluginManager.pluginList}" var="plugin">
	<c:set var="plugin" value="${plugin}" scope="request"/>
	
	<%
		Plugin p = (Plugin) request.getAttribute("plugin");
		Localizer l = Localization.getLocalizer(p.getName());
		request.setAttribute("l", l);
	%>

	<c:set var="page" value="${plugin.endpoints[endpoint]}"/>
	<c:if test="${not empty page}">
		<jsp:include page="${page}"/>
	</c:if>
</c:forEach>