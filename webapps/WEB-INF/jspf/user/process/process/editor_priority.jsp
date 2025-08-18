<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Выберите приоритет')}</h1>
<html:form action="/user/process">
	<html:hidden property="id"/>
	<input type="hidden" name="method" value="processPriorityUpdate"/>

	<c:forEach var="item" items="${config.priorityColors}">
		<div style="background-color: ${item.value};" class="pl05 pt05 pb05">
			<html:radio property="priority" value="${item.key}"/>
			<c:set var="description" value="${config.getPriorityDescription(item.key)}"/>
			<div style="display: inline; padding-top: 0.15em;" class="pl05">${item.key}${empty description ? '' : ' - '.concat(description)}</div>
		</div>
	</c:forEach>
	<div class="pt1 pb1">
		<%@ include file="editor_save_cancel.jsp"%>
	</div>
</html:form>
