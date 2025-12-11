<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Создание счёта')}</h1>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/invoice/invoice" styleId="${uiid}">
	<html:hidden property="method"/>
	<html:hidden property="processId"/>
	<html:hidden property="returnUrl"/>

	<div class="in-inline-block mb1">
		<ui:combo-single list="${types}" name="typeId" value="${typeId}" prefixText="${l.l('Type')}:" widthTextValue="10em"/>
		&nbsp;
		<ui:date-month name="monthFrom" value="${monthFrom}"/>
		&nbsp;
		<ui:date-month name="monthTo" value="${monthTo}"/>
	</div>

	<div>
		<ui:button type="ok" styleClass="mr1" onclick="$$.ajax.load(this.form, $('#${uiid}').parent())"/>
		<ui:button type="cancel" onclick="$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent())"/>
	</div>
</html:form>
