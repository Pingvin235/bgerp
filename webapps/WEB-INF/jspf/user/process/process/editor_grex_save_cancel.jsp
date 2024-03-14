<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="closeEditor">$$.ajax.load('${form.returnUrl}', $('#${form.returnChildUiid}').parent());</c:set>
<c:set var="saveCommand">$$.ajax.post(this).done(() => { ${closeEditor} })</c:set>

<div class="mt1">
	<button class="btn-grey" type="button" onclick="${saveCommand}">OK</button>
	<button class="btn-white ml1" type="button" onclick="${closeEditor}">${l.l('Cancel')}</button>
</div>