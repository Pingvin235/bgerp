<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="status" value="${form.response.data.status}"/>

<html:form action="admin/process" styleClass="center500">
	<input type="hidden" name="action" value="statusUpdate"/>
	
	<h2>ID</h2>
	<input type="text" name="id" value="${form.param['id']}" disabled="disabled" style="width: 100%;"/> 

	<h2>${l.l('Название')}</h2>
	<html:text property="title" style="width: 100%" value="${status.title}"/>
		
	<h2>${l.l('Позиция')}</h2>
	<html:text property="pos" style="width: 100%" value="${status.pos}"/>
	<div class="hint">${l.l('Позиция статуса в различных перечнях')}.</div>
	
	<div class="mt1">		
		<%@ include file="/WEB-INF/jspf/send_and_cancel_form.jsp"%>
	</div>
</html:form>

<shell:state ltext="${l.l('Редактор')}" help="kernel/process/index.html#status"/>