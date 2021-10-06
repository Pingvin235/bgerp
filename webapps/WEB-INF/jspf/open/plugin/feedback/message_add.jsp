<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="config" value="${ctxSetup.getConfig('org.bgerp.plugin.msg.feedback.action.open.MessageAction$Config')}"/> 
<c:if test="${not empty config}">
	<c:url var="url" value="/open/plugin/feedback/message.do">
		<c:param name="action" value="edit"/>
		<c:param name="processId" value="${form.id}"/>
	</c:url>
	<button class="btn-green" 
		onclick="$$.ajax.load('${url}', $(this).parent())" 
		title="${l.l('Написать сообщение')}">+</button>
</c:if>