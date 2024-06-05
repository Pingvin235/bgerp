<%@ tag body-content="empty" pageEncoding="UTF-8" description="Пункт меню"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="action" description="Action identifier" required="true"%>
<%@ attribute name="title" description="Title"%>
<%@ attribute name="icon" description="Font icon"%>
<%-- only href or command can be used --%>
<%@ attribute name="href" description="Tool's URL to be opened in UI"%>
<%@ attribute name="command" description="JS command or action call URL"%>

<%-- making sure href starting from '/user' --%>
<c:if test="${not empty href and not href.startsWith('/user')}">
	<c:set var="href" value="/user/${href}"/>
</c:if>

<c:set var="allowed" value="${ctxUser.checkPerm(action)}"/>
<c:if test="${allowed}">
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

<c:if test="${not empty href}">
	<c:set var="menuItemsJS" scope="request">
		${menuItemsJS}
		<c:if test="${not empty icon}">
			menuItems.icons.push('${icon}');
		</c:if>
		menuItems.add({href: '${href}', action: '${u:actionUrl(action)}', title: '${title}', allowed: ${allowed}});
		<c:if test="${not empty icon}">
			menuItems.icons.pop();
		</c:if>
	</c:set>
</c:if>
