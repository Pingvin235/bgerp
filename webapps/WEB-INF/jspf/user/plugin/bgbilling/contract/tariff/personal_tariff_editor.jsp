<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="personalTariff" value="${frd.personalTariff}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/contractTariff" styleId="${uiid}">
	<input type="hidden" name="method" value="updatePersonalTariff"/>
	<html:hidden property="billingId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="id"/>
	<input type="hidden" name="position" value="${ personalTariff.pos }" />

	<div class="in-table-cell mb1">
		<div style="width: 100%;">
			<input type="text" name="title" value="${personalTariff.title}" style="width: 100%;"/>
		</div>

		<div style="white-space: nowrap;" class="pl1">
			c
			<ui:date-time paramName="dateFrom" value="${tu.format(personalTariff.date1, 'ymd')}"/>
			по
			<ui:date-time paramName="dateTo" value="${tu.format(personalTariff.date2, 'ymd')}"/>
		</div>
	</div>

	<%@ include file="/WEB-INF/jspf/ok_cancel_in_form.jsp"%>
</html:form>
