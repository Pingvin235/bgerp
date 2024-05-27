<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<jsp:useBean id="now" class="java.util.Date" scope="page"/>
<c:set var="currentDate" value="${tu.format(now, 'dd.MM.yyyy'}"/>

<html:form styleId="${uiid}" action="/user/plugin/bgbilling/proto/contractTariff" style="width: 100%;">
	<input type="hidden" name="method" value="updateContractTariff"/>
	<c:if test="${not empty stepData.contractTariff}">
		<input type="hidden" name="id" value="${stepData.contractTariff.id}"/>
	</c:if>
	<input type="hidden" name="dateFrom" value="${currentDate}"/>
	<input type="hidden" name="contractId" value="${stepData.contract.id}"/>
	<input type="hidden" name="billingId" value="${stepData.contract.billingId}"/>
	<input type="hidden" name="comment" value="Мастер"/>
	<div class="in-table-cell">
		<ui:combo-single hiddenName="tariffPlanId" list="${stepData.tariffList}" value="${stepData.contractTariff.tariffPlanId}"/>
		<button class="ml1 btn-white" type="button" onClick="if (this.form.tariffPlanId.value > 0) $$.ajax.post(this.form).done(() => { ${reopenProcessEditorCode} })">Выбрать</button>
	</div>
</html:form>