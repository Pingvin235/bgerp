<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mt1 mb1">
	<ui:button type="ok" onclick="
		if (this.form.checkEmptySubject && !$$.message.checkSubject(this.form, '${l.l('Save with the empty subject?')}'))
			return;
		if (!$$.message.checkAttach(this.form, '${l.l('Save without attachment?')}'))
			return;
		$$.ajax.post(this).done(() => { $$.ajax.load('${form.returnUrl}', $('#${form.returnChildUiid}').parent()) })
	"/>
	<ui:button type="cancel" onclick="$('#${form.returnChildUiid}').empty();" styleClass="ml1"/>
</div>
