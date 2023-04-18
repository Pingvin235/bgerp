<%@ tag body-content="empty" pageEncoding="UTF-8" description="Add endpoints from plugins"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="endpoint" description="Endpoint"%>

<%@tag import="org.bgerp.l10n.Localization"%>
<%@tag import="ru.bgcrm.plugin.Plugin"%>

<%
	final String KEY = ru.bgcrm.servlet.filter.SetRequestParamsFilter.REQUEST_KEY_LOCALIZER;
	Object lBefore = request.getAttribute(KEY);
%>

<c:forEach items="${ctxPluginManager.pluginList}" var="plugin">
	<%-- pageContext isn'n available for tag files --%>
	<c:set var="plugin" value="${plugin}" scope="request"/>
	<c:set var="endpoints" value="${plugin.getEndpoints(endpoint)}"/>

	<c:if test="${not empty endpoints}">
		<%
			Plugin p = (Plugin) request.getAttribute("plugin");
			request.setAttribute(KEY, Localization.getLocalizer(Localization.getLang(request), p.getId()));
		%>

		<c:forEach items="${endpoints}" var="page">
			<jsp:include page="${page}"/>
		</c:forEach>
	</c:if>
</c:forEach>

<%
	if (lBefore != null)
		request.setAttribute(KEY, lBefore);
	else
		request.removeAttribute(KEY);
%>