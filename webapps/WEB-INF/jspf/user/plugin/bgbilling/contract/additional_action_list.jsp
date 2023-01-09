<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<form action="/user/plugin/bgbilling/proto/contract.do" class="in-table-cell" id="${uiid}">
	<input type="hidden" name="action" value="executeAdditionalAction" />
	<input type="hidden" name="billingId" value="${form.param.billingId}" />
	<input type="hidden" name="contractId" value="${form.param.contractId}" />

	<div class="pr1" style="width: 100%;">
		<ui:select-single list="${form.response.data.additionalActionList}" hiddenName="actionId" value="${form.param.actionId}"
			placeholder="Выберите действие" style="width: 100%;"/>
	</div>
	<div>
		<button class="btn-grey" type="button" title="Выполнить действие" onClick="openUrlToParent( formUrl( this.form ), $('#${uiid}') )">Выполнить</button>
	</div>
</form>

<c:set var="result" value="${form.response.data.executeResult}"/>
<c:if test="${not empty result}">
	<h2>Результат</h2>

	<div class="box p05" style="overflow: auto; width: inherit; height: 550px;">
		<pre>${result}</pre>
	</div>
</c:if>

<%-- <b>Отчет</b>
<div class="box" style="overflow: auto; width: inherit; height: 550px;">
	<textarea rows="10" cols="300" style="width: 100%; height: 100%;" readonly></textarea>
</div> --%>
