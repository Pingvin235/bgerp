<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="status" value="${form.response.data.status}"/>

<html:form action="admin/process" styleClass="center500">
	<input type="hidden" name="action" value="statusUpdate"/>
	
	<h2>ID</h2>
	<input type="text" name="id" value="${form.param['id']}" disabled="disabled" style="width: 100%;"/> 

	<h2>Название</h2>
	<html:text property="title" style="width: 100%" value="${status.title}"/>
		
	<h2>Позиция</h2>
	<html:text property="pos" style="width: 100%" value="${status.pos}"/>
	<div class="hint">Позиция статуса в различных перечнях.</div>
	
	<div class="mt1">		
		<%@ include file="/WEB-INF/jspf/send_and_cancel_form.jsp"%>
	</div>
</html:form>

<c:set var="state" value="Редактор"/>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/process/index.html#status"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>