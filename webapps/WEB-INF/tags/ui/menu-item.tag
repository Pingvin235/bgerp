<%@ tag body-content="empty" pageEncoding="UTF-8" description="Пункт меню"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>


<%@ attribute name="href" description="Ссылка"%>
<%@ attribute name="action" description="Action class"%>
<%@ attribute name="command" description="Команда"%>
<%@ attribute name="title" description="Title"%>
<%@ attribute name="ltitle" description="Title to be localized"%>
<%@ attribute name="hidden" description="скрывать" %>

<c:if test="${not empty ltitle}">
	<c:set var="title" value="${l.l(ltitle)}"/>
</c:if>

<c:if test="${not empty href and not fn:startsWith(href, '/user')}">
	<c:set var="href" value="/user/${href}"/>
</c:if>

<c:set var="allowed" value="false"/>
<p:check action="${action}">
	<c:set var="allowed" value="true"/>
	<c:if test="${empty hidden}">
		<li>
			<%-- если есть href - то в command содержится HTTP запрос, 
				 если нет - то JS код --%>
			<c:choose>
				<c:when test="${not empty href}">
					<a href="${href}" onclick="bgerp.shell.followLink(this.href, event)">${title}</a>
				</c:when>
				<c:otherwise>
					<a href="#" onclick="${command}; return false;">${title}</a>
				</c:otherwise>
			</c:choose>		
		</li>	
	</c:if>
</p:check>

<c:if test="${not empty href}">
	<c:set var="menuItemsJS" scope="request">
		${menuItemsJS}
		menuItems['${href}'] = {action: '${command}', title: '${title}', allowed: ${allowed}};
	</c:set>
</c:if>
