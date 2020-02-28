<%@ tag body-content="empty" pageEncoding="UTF-8" description="Ссылка на открытие процесса"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="User ID" required="true" type="java.lang.Integer"%>

<c:if test="${not empty id}"><%--
	--%><c:choose><%--
		--%><c:when test="${id eq ctxUser.id}"><%--
			--%><c:set var="href" value="/user/profile"/><%--
		--%></c:when><%--
		--%><c:otherwise><%--
			--%><c:set var="href" value="/user/userProfile#${id}"/><%--
		--%></c:otherwise><%--
	--%></c:choose><%--
	--%><a href="${href}" onclick="bgerp.shell.followLink(this.href, event)">${ctxUserMap[id].title}</a><%--
--%></c:if>
