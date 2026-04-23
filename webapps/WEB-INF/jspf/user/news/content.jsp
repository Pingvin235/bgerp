<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="item" value="${frd.item}"/>

<div title="${item.title}">
	<%-- Не используется u:htmlEncode, т.к. иначе сломается поддержка HTML! --%>
	<% pageContext.setAttribute("newLineChar", "\n"); %>

	<p>${item.text.replace(newLineChar, "<br/>")}</p>

	<p><b>${l.l('Author')}:</b> ${ctxUserMap[item.userId].title}</p>
	<p><b>${l.l('Time')}:</b> ${tu.format(item.createTime, 'ymdhms')}</p>
</div>