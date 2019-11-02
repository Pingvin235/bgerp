<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:menu-item title="${title}" href="${href}" action="${action}"
	command="${command}" hidden="${hidden}" />

<c:remove var="href" />
<c:remove var="action" />
<c:remove var="command" />
<c:remove var="title" />
<c:remove var="hidden" />