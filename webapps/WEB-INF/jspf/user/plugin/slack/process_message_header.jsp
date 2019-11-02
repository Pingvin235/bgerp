<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${messageType.getClass().getName() eq 'ru.bgcrm.plugin.slack.dao.MessageTypeChannel'}">
	<div style="width: 100%;">
		 <div>
		 	#${message.id} Slack 
		 	Создано: ${u:formatDate( message.fromTime, 'ymdhm' )}
		 	<c:set var="userId" value="${message.userId}"/>
            (<%@ include file="/WEB-INF/jspf/user_link.jsp"%>)
		</div>
	</div>
</c:if>