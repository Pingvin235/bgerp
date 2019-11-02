<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- выводится во всплывающем окне --%>
<c:if test="${empty item }">
	<c:set var="item" value="${form.response.data.item}"/>
</c:if>

<%-- Не используется u:htmlEncode, т.к. иначе сломается поддержка HTML! --%>
<% pageContext.setAttribute("newLineChar", "\n"); %>

<p><b>${item.title}</b></p>
<p>${fn:replace(item.description, newLineChar, "<br/>")}</p>

<c:if test="${empty hideNewsHeaders}">
	<p><b>Автор:</b> ${ctxUserMap[item.userId].title}</p>
	<p><b>Дата:</b> ${u:formatDate(item.createDate, 'ymdhms')}</p>
</c:if>
