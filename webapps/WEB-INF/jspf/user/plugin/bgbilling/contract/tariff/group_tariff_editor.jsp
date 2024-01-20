<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="tariffGroup" value="${frd.tariffGroup}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/contractTariff" styleId="${uiid}">
	<input type="hidden" name="action" value="updateContractTariffGroup"/>
	<html:hidden property="billingId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="id"/>

	<div class="in-table-cell mb1">
		<div style="width: 100%;">
			<ui:select-single list="${frd.registredTariffGroupList}" hiddenName="tariffGroupId" value="${tariffGroup.groupId}"
				style="width: 100%;" placeholder="Группа"/>
		</div>
		<div style="white-space:nowrap;" class="pl1">
			c
			<ui:date-time paramName="dateFrom" value="${tu.format(tariffGroup.dateFrom, 'ymd')}"/>
			по
			<ui:date-time paramName="dateTo" value="${tu.format(tariffGroup.dateTo, 'ymd')}"/>
		</div>
	</div>

	Комментарий:
	<textarea name="comment" rows="4" cols="10" style="width:100%; resize: vertical;">${tariffGroup.comment}</textarea>

	<%@ include file="/WEB-INF/jspf/ok_cancel_in_form.jsp"%>
</html:form>