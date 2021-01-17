<%@ tag body-content="empty" pageEncoding="UTF-8" description="Link for opening a process"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="Process ID" type="java.lang.Integer"%>
<%@ attribute name="process" description="Process object" type="ru.bgcrm.model.process.Process"%>
<%@ attribute name="text" description="Optional link text, if not defined - used process ID"%>

<c:if test="${not empty process}">
	<c:set var="id" value="${process.id}"/>
</c:if>

<c:set var="text">
	<c:choose>
		<c:when test="${not empty text}">${text}</c:when>
		<c:otherwise>${id}</c:otherwise>
	</c:choose>
</c:set>

<ui:when type="user">
	<a href="/user/process#${id}" onclick="$$.process.open(${id}); return false;">${text}</a>
</ui:when>
<ui:when type="open">
	<c:set var="config" value="${u:getConfig(ctxSetup, 'org.bgerp.action.open.ProcessAction$Config')}"/>
	<c:if test="${config.isOpen(process)}"><%--
	--%><a id="${id}" href="/open/process/${id}">${text}</a><%--
	--%></c:if>
</ui:when>