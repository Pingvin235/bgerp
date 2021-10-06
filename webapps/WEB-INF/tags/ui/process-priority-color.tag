<%@ tag body-content="empty" pageEncoding="UTF-8" description="Link for opening a process"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="priority" description="Priority number" type="java.lang.Integer" required="true"%>

<c:set var="config" value="${ctxSetup.getConfig('ru.bgcrm.model.process.config.ProcessPriorityConfig')}"/>

${config.priorityColors[priority]}