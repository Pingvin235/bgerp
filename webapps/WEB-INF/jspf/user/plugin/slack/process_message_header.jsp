<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${messageType.getClass().getName() eq 'ru.bgcrm.plugin.slack.dao.MessageTypeChannel'}">
	<div style="width: 100%;">
		 <div>
		 	#${message.id} Slack 
		 	${l.l('Создано')}: ${u:formatDate( message.fromTime, 'ymdhm' )} (<ui:user-link id="${message.userId}"/>)
		</div>
	</div>
</c:if>