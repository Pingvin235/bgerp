<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="width: 100%;">
	<div>
		#${message.id}&nbsp;${messageType.title}&nbsp;<b><a target="_blank" href="${messageType.getPostUrl(message.systemId)}">${message.subject}</a></b>
	</div>
	<div class="mt05">						 
		Изменение: ${tu.format( message.fromTime, 'ymdhm' )}
	</div>
</div>