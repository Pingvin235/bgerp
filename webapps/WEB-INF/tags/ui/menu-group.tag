<%@ tag body-content="scriptless" pageEncoding="UTF-8" description="Menu group"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="title" description="Title"%>
<%@ attribute name="ltitle" description="Title to be localized"%>

<c:if test="${not empty ltitle}">
	<c:set var="title" value="${l.l(ltitle)}"/>
</c:if>

<li>
	<a href="#">${title}</a>
	<ul>
		<jsp:doBody/>
	</ul>
</li>
