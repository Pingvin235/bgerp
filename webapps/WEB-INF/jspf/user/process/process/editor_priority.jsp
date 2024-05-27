<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Выберите приоритет')}</h1>
<html:form action="/user/process">
	<html:hidden property="id"/>
	<input type="hidden" name="method" value="processPriorityUpdate"/>

	<c:set var="config" value="${ctxSetup.getConfig('ru.bgcrm.model.process.config.ProcessPriorityConfig')}"/>

	<c:forEach var="item" items="${config.priorityColors}">
		<div style="background-color: ${item.value};" class="pl05 pt05 pb05">
			<html:radio property="priority" value="${item.key}"/>
			<div style="display: inline; padding-top: 0.15em;" class="pl05">${item.key}</div>
		</div>
	</c:forEach>

	<%@ include file="editor_save_cancel.jsp"%>
</html:form>