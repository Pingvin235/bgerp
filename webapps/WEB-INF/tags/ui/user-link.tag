<%@ tag body-content="empty" pageEncoding="UTF-8" description="Ссылка на открытие процесса"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="User ID" required="true" type="java.lang.Integer"%>
<%@ attribute name="text" description="Text instead of user title"%>

<c:if test="${not empty id}"><%--
	--%><a href="/user/profile#${id}" onclick="$$.shell.followLink(this.href, event)">${not empty text ? text : ctxUserMap[id].title}</a><%--
--%></c:if>
