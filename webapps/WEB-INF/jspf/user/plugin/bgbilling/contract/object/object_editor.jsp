<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<h1>
	Редактор <span class="normal">[<a href="#" onclick="$$.ajax.load('${form.returnUrl}', document.getElementById('${uiid}').parentElement); return false;">закрыть</a>]</span>
</h1>

<form id="${uiid}" action="${form.requestURI}" class="mb1">
	<c:set var="object" value="${frd.object}"/>

	<input type="hidden" name="method" value="updateContractObject"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>
	<input type="hidden" name="contractId" value="${form.param.contractId}"/>
	<input type="hidden" name="objectId" value="${object.id}"/>
	<input type="hidden" name="typeId" value="${object.typeId}"/>

	<div style="display: flex;">
		<div class="nowrap">
			Период c
			<ui:date-time paramName="dateFrom" value="${tu.format(object.dateFrom, 'ymd')}"/>
			по
			<ui:date-time paramName="dateTo" value="${tu.format(object.dateTo, 'ymd')}"/>
			Название&nbsp;
		</div>
		<input type="text" style="width: 100%;" name="title" value="${object.title}"/>
		<button class="btn-grey ml1" onclick="$$.ajax.post(this).done(() => alert('Изменения произведены успешно!'))">Применить</button>
	</div>
</form>

<c:set var="tabsUiid" value="${u:uiid()}"/>
<div id="${tabsUiid}">
	<ul></ul>
</div>

<script>
	$(function() {
		const $objectEditorTabs = $("#${tabsUiid}").tabs({refreshButton: true});

		<c:url var="url" value="${form.requestURI}">
			<c:param name="method" value="contractObjectParameterList"/>
			<c:param name="billingId" value="${form.param.billingId}"/>
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="objectId" value="${form.param.objectId}"/>
		</c:url>
		$objectEditorTabs.tabs("add", "${url}", "Параметры объекта");

		<c:url var="url" value="${form.requestURI}">
			<c:param name="method" value="contractObjectModuleSummaryTable"/>
			<c:param name="billingId" value="${form.param.billingId}"/>
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="objectId" value="${form.param.objectId}"/>
		</c:url>
		$objectEditorTabs.tabs("add", "${url}", "Модули объекта");
	});
</script>