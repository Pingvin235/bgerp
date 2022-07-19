<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mt1 mb1">
	<ui:button type="ok" onclick="$$.ajax.post(this).done(() => { $$.ajax.load('${form.returnUrl}', $('#${form.returnChildUiid}').parent()) })"/>
	<ui:button type="cancel" onclick="$('#${form.returnChildUiid}').empty();" styleClass="ml1"/>
</div>
