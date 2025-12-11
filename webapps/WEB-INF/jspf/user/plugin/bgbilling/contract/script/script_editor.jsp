<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="script" value="${frd.script}"/>

<h1>Редактор</h1>

<form action="/user/plugin/bgbilling/proto/contract.do" id="${uiid}">
	<input type="hidden" name="method" value="updateScript"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>
	<input type="hidden" name="contractId" value="${form.param.contractId}"/>
	<input type="hidden" name="scriptId" value="${script.id}"/>

	<div class="in-table-cell mb1">
		<div style="width: 100%;">
			<ui:select-single list="${frd.scriptTypeList}" hiddenName="scriptTypeId" value="${script.scriptId}"
				style="width: 100%;" placeholder="Скрипт"/>
		</div>
		<div style="white-space:nowrap;" class="pl1">
			c
			<ui:date-time name="dateFrom" value="${tu.format(script.period.dateFrom, 'dd.MM.yyyy')}"/>
			по
			<ui:date-time name="dateTo" value="${tu.format(script.period.dateTo, 'dd.MM.yyyy')}"/>
		</div>
	</div>

	Комментарий:
	<textarea name="comment" rows="4" cols="10" style="width:100%; resize: vertical;">${script.comment}</textarea>

	<div class="mt1">
		<button type="button" class="btn-grey" onclick="$$.ajax.post(this.form).done(() => $$.ajax.load('${form.returnUrl}', $('#${uiid}').parent()))">OK</button>
		<button type="button" class="btn-grey ml1" onclick="$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent());">Oтмена</button>
	</div>
</form>
