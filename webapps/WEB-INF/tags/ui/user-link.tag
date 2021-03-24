<%@ tag body-content="empty" pageEncoding="UTF-8" description="Link for opening of user"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="User ID" required="true" type="java.lang.Integer"%>
<%@ attribute name="text" description="Text instead of user title"%>

<ui:when type="user">
	<c:if test="${not empty id}"><%--
		--%><a href="/user/profile#${id}" onclick="$$.shell.followLink(this.href, event)">${not empty text ? text : ctxUserMap[id].title}</a><%--
--%></c:if>
</ui:when>
<ui:when type="open">
	<c:set var="config" value="${u:getConfig(ctxSetup, 'org.bgerp.action.open.ProfileAction$Config')}"/>
	<c:choose>
		<c:when test="${config.isUserShown(id)}"><%--
		--%><a href="/open/profile/${id}">${ctxUserMap[id].title}</a><%--
	--%></c:when>
		<c:otherwise>User ${u.getDigest(ctxUserMap[id].title).substring(20)}</c:otherwise>
	</c:choose>
</ui:when>
