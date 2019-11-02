<%@page import="ru.bgerp.i18n.Localization"%>
<%@page import="ru.bgcrm.plugin.Plugin"%>
<%@page import="ru.bgerp.i18n.Localizer"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

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