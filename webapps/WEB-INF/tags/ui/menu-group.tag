<%@ tag body-content="scriptless" pageEncoding="UTF-8" description="Menu group"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="title" description="Title"%>
<%@ attribute name="ltitle" description="Title to be localized"%>
<%@ attribute name="hidden" description="Hidden menu item"%>
<%@ attribute name="actionFactory" description="Actions generator"%>

<c:if test="${not empty ltitle}">
	<c:set var="title" value="${l.l(ltitle)}"/>
</c:if>

<li>
	<a href="#">${title}</a>
	<ul>
		<c:if test="${not empty actionFactory}">
			<c:forEach var="action" items="${u:newInstance(actionFactory).create(l)}">
				<ui:menu-item title="${action.getTitle(l)}" 
					href="${action.href}"
					action="${action.action}"
					command="${action.actionUrl}"/>
			</c:forEach>
		</c:if>
		<jsp:doBody/>
	</ul>
</li>
