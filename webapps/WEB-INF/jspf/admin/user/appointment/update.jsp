<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="appointment" value="${form.response.data.appointment}" />

<html:form action="admin/appointment" styleId="appointmentEditForm" styleClass="center1020">
	<input type="hidden" name="action" value="appointmentUpdate" />
	<html:hidden property="id" />

	<h2>ID</h2>
	<input type="text" disabled="disabled" disabled="true" style="width: 100%" value="${form.id}"/>
	<h2>${l.l('Наименование')}</h2>
	<html:text property="title" value="${appointment.title }" style="width:100%;"/>
	<h2>${l.l('Комментарий')}</h2>
	<html:textarea property="description" rows="3" value="${appointment.description}" style="width:100%"/>

	<ui:form-ok-cancel styleClass="mt1"/>
</html:form>

<c:set var="state" value="${l.l('Редактор')}"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>