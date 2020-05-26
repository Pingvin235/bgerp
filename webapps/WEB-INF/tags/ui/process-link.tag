<%@ tag body-content="empty" pageEncoding="UTF-8" description="Ссылка на открытие процесса"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="Process ID" required="true" type="java.lang.Integer"%>
<%@ attribute name="process" description="Process itself" type="ru.bgcrm.model.process.Process"%>
<%@ attribute name="text" description="Опционально - текст ссылки, если не указан - то используется код процесса"%>

<ui:when type="user">
	<a href="/user/process#${id}" onclick="$$.process.open(${id}); return false;"><%--
	--%><c:choose>
			<c:when test="${not empty text}">${text}</c:when>
			<c:otherwise>${id}</c:otherwise>
		</c:choose><%--
	--%></a><%--
--%></ui:when>
<ui:when type="open">
	<c:set var="config" value="${u:getConfig(ctxSetup, 'org.bgerp.action.open.ProcessAction$Config')}"/>
	<c:if test="${config.getProcessTypeIds().contains(process.typeId)}"><%--
	--%><a id="${process.id}" href="/open/process/${process.id}">${process.id}</a><%--
	--%></c:if>
</ui:when>