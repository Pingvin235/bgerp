<%@ tag body-content="scriptless" pageEncoding="UTF-8" description="Menu group"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="title" description="Title"%>
<%@ attribute name="ltitle" description="Title to be localized"%>
<%@ attribute name="hidden" description="Hidden menu item"%>
<%@ attribute name="actionFactory" description="Actions generator"%>
<%@ attribute name="icon" description="Inherited font icon"%>

<c:if test="${not empty ltitle}">
	<c:set var="title" value="${l.l(ltitle)}"/>
</c:if>

<li>
	<a href="#">
		<ui:menu-icon icon="${icon}"/>
		${title}
	</a>
	<ul>
		<c:if test="${not empty actionFactory}">
			<c:forEach var="action" items="${u:newInstance(actionFactory).create(l)}">
				<ui:menu-item title="${action.getTitle(l)}"
					href="${action.href}"
					action="${action.action}"
					command="${action.actionUrl}"/>
			</c:forEach>
		</c:if>

		<c:set var="menuItemsJS" scope="request">
			${menuItemsJS}
			menuItems.titles.push('${title}');
			<c:if test="${not empty icon}">
				menuItems.icons.push('${icon}');
			</c:if>
		</c:set>

		<jsp:doBody/>

		<c:set var="menuItemsJS" scope="request">
			${menuItemsJS}
			menuItems.titles.pop();
			<c:if test="${not empty icon}">
				menuItems.icons.pop();
			</c:if>
		</c:set>
	</ul>
</li>

