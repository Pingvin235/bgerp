<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>Редактор</h1>

<form action="/user/plugin/bgbilling/proto/contract.do">
	<input type="hidden" name="action" value="updateMemo" />
	<input type="hidden" name="billingId" value="${form.param.billingId}" />
	<input type="hidden" name="contractId" value="${form.param.contractId}" />
	<input type="hidden" name="id" value="${form.param.id}" />

	<c:set var="memo" value="${form.response.data.memo}"/>

	<h2>Тема</h2>
	<input type="text" name="title" style="width:100%" value="${memo.title}"/>

	<h2>Текст</h2>
	<textarea name="text" rows="10" style="width:100%;">${memo.text}</textarea>

	<c:set var="returnCommand" value="$$.ajax.load('${form.returnUrl}',$(this.form).parent());"/>
	<div class="mt1">
		<button type="button" class="btn-grey" onclick="if( sendAJAXCommand( formUrl(this.form) ) ){ ${returnCommand} }">OK</button>
		<button type="button" class="btn-grey ml1" onclick="${returnCommand}">Отмена</button>
	</div>
</form>