<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mt1">
	<c:set var="returnCommand" value="openUrlToParent( '${form.returnUrl}', $(this.form) )"/>
	<button type="button" class="btn-grey" onclick="if(sendAJAXCommand(formUrl(this.form)) ){ ${returnCommand} }">OK</button>
	<button type="button" class="btn-grey ml1" onclick="${returnCommand}">${l.l('Отмена')}</button>
</div>