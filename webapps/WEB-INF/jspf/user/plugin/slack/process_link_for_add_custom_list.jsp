<%@page import="ru.bgcrm.dao.message.config.MessageTypeConfig"%>
<%@page import="ru.bgcrm.util.Setup"%>
<%@page import="ru.bgcrm.plugin.slack.dao.MessageTypeChannel"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%
   MessageTypeChannel type = Setup.getSetup().getConfig(MessageTypeConfig.class).getMessageType(MessageTypeChannel.class);
   if (type != null)
	   pageContext.setAttribute("slackMessageType", type);
%>

<c:if test="${not empty slackMessageType}">
	<c:set var="id" value="${u:uiid()}"/>
	
	<c:set var="linkObjectItems" scope="request">
		${linkObjectItems}
		<li value="${id}">Канал Slack</li>
	</c:set>
	<c:set var="linkObjectForms" scope="request"> 
		${linkObjectForms}
		<form action="/user/plugin/slack/channel.do" id="${id}" style="display: none;">
			<input type="checkbox" name="check" style="display: none;" checked="true"/>
			<input type="hidden" name="processId" value="${form.id}"/>
			<input type="hidden" name="action" value="addProcessChannelLink"/>
			<input style="width: 100%;" name="channelTitle" placeholder="Имя канала"/>
		</form>
	</c:set>
</c:if>	