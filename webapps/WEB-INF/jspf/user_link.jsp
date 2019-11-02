<%@ page contentType="text/html; charset=UTF-8"%><%-- 
--%><%@ include file="/WEB-INF/jspf/taglibs.jsp"%><%--

--%><c:if test="${not empty userId}"><%--
	--%><c:choose><%--
		--%><c:when test="${userId eq ctxUser.id}"><%--
			--%><c:set var="href" value="profile"/><%--
		--%></c:when><%--
		--%><c:otherwise><%--
			--%><c:set var="href" value="/user/userProfile#${userId}"/><%--
		--%></c:otherwise><%--
	--%></c:choose><%--
	--%><a href="${href}" onclick="bgerp.shell.followLink(this.href, event)">${ctxUserMap[userId].title}</a><%--
--%></c:if>