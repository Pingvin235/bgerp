<%@ tag body-content="scriptless" pageEncoding="UTF-8" description="Menu group"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="title" description="Title"%>
<%@ attribute name="actionFactory" description="Actions generator"%>
<%@ attribute name="icon" description="Inherited font icon"%>

<li>
	<a href="#">
		<ui:menu-icon icon="${icon}"/>
		${title}
	</a>
	<ul>
		<c:set var="menuItemsJS" scope="request">
			${menuItemsJS}
			menuItems.titles.push('${title}');
			<c:if test="${not empty icon}">
				menuItems.icons.push('${icon}');
			</c:if>
		</c:set>

		<c:if test="${not empty actionFactory}">
			<c:forEach var="action" items="${u.newInstance(actionFactory).create()}">
				<ui:menu-item title="${action.getTitle()}" href="${action.href}" action="${action.action}"/>
			</c:forEach>
		</c:if>

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

