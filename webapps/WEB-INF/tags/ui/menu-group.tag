<%@ tag body-content="empty" pageEncoding="UTF-8" description="Группа меню"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="title" description="Title"%>
<%@ attribute name="ltitle" description="Title to be localized"%>
<%@ attribute name="subitems" description="Subitems"%>

<c:if test="${not empty ltitle}">
	<c:set var="title" value="${l.l(ltitle)}"/>
</c:if>

<c:if test="${not empty subitems}">
<li>
	<a href="#">${title}</a>
	<ul>
		${subitems}
	</ul>
</li>
</c:if>
