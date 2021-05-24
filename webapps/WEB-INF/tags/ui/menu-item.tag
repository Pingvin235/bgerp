<%@ tag body-content="empty" pageEncoding="UTF-8" description="Пункт меню"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="href" description="URL"%>
<%@ attribute name="action" description="Action class"%>
<%@ attribute name="command" description="Command"%>
<%@ attribute name="title" description="Title"%>
<%@ attribute name="ltitle" description="Title to be localized"%>
<%@ attribute name="hidden" description="Hidden menu item"%>
<%@ attribute name="icon" description="Font icon"%>

<c:if test="${not empty ltitle}">
	<c:set var="title" value="${l.l(ltitle)}"/>
</c:if>

<%-- if href presents - then command contains action's URL, else - JS code --%>

<c:if test="${not empty href and not href.startsWith('/user')}">
	<c:set var="href" value="/user/${href}"/>
</c:if>

<c:set var="allowed" value="false"/>
<p:check action="${action}">
	<c:set var="allowed" value="true"/>
	<c:if test="${empty hidden}">
		<li>
			<c:choose>
				<c:when test="${not empty href}">
					<a href="${href}" onclick="$$.shell.followLink(this.href, event)">
				</c:when>
				<c:otherwise>
					<a href="#" onclick="${command}; return false;">
				</c:otherwise>
			</c:choose>
			<ui:menu-icon icon="${icon}"/>
			${title}</a>
		</li>
	</c:if>
</p:check>

<c:if test="${not empty href}">
	<c:set var="menuItemsJS" scope="request">
		${menuItemsJS}
		<c:if test="${not empty icon}">
			menuItems.icons.push('${icon}');
		</c:if>
		menuItems.add({href: '${href}', action: '${command}', title: '${title}', allowed: ${allowed}});
		<c:if test="${not empty icon}">
			menuItems.icons.pop();
		</c:if>
	</c:set>
</c:if>
