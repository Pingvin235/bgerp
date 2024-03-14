<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mt1">
	<c:set var="returnCommand" value="$$.ajax.load('${form.returnUrl}', $(this.form).parent())"/>
	<button type="button" class="btn-grey" onclick="$$.ajax.post(this.form).done(() => { ${returnCommand} })">OK</button>
	<button type="button" class="btn-grey ml1" onclick="${returnCommand}">${l.l('Cancel')}</button>
</div>