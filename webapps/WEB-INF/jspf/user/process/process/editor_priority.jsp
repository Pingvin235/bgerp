<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>Выберите приоритет</h1>
<html:form action="/user/process">
	<html:hidden property="id"/>
	<input type="hidden" name="action" value="processPriorityUpdate"/>
		
	<c:forEach begin="0" end="9" varStatus="status">
		<c:set var="priority" value="${status.index}"/>
		<%@ include file="/WEB-INF/jspf/process_color.jsp"%>
		
		<div style="background-color: ${color};" class="pl05 pt05 pb05">
			<html:radio property="priority" value="${status.index}"/>
			<div style="display: inline; padding-top: 0.15em;" class="pl05">${status.index}</div>
		</div>	
	</c:forEach>
	
	<table style="width: 100%;">	
		<%@ include file="editor_save_cancel_tr.jsp"%>
	</table>
</html:form>