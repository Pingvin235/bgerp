<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${messageType.getClass().name.endsWith('MessageTypeEmail')}">
	<%@ include file="/WEB-INF/jspf/user/message/process_message_attachments.jsp"%>
</c:if>