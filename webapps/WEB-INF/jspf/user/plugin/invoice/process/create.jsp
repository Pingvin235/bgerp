<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Создание счёта')}</h1>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/invoice/invoice" styleId="${uiid}">
	<html:hidden property="action"/>
	<html:hidden property="processId"/>
	<html:hidden property="returnUrl"/>

	<div class="in-inline-block mb1">
		<div><ui:date-month/></div>
		<div class="pl1"><ui:combo-single list="${types}" hiddenName="typeId" prefixText="${l.l('Type')}:" widthTextValue="10em"/></div>
	</div>

	<div>
		<ui:button type="ok" styleClass="mr1" onclick="$$.ajax.load(this.form, $('#${uiid}').parent())"/>
		<ui:button type="cancel" onclick="$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent())"/>
	</div>
</html:form>
