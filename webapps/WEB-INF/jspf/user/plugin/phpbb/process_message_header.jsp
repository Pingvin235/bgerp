<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${messageType.getClass().getName() eq 'ru.bgcrm.plugin.phpbb.dao.MessageTypeForumPost'}">
	<div style="width: 100%;">
		 <div>
		 	#${message.id}&nbsp;${messageType.title}&nbsp;<b><a target="_blank" href="${messageType.getPostUrl(message.systemId)}">${message.subject}</a></b>
		 </div>
		 <div class="mt05">						 
			Изменение: ${u:formatDate( message.fromTime, 'ymdhm' )}
		</div>
	</div>	
</c:if>