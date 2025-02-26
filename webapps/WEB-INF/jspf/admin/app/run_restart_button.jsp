<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:combo-single hiddenName="force" widthTextValue="5em" prefixText="${l.l('Restart')}:">
	<jsp:attribute name="valuesHtml">
		<li value="0">${l.l('Нормальный')}</li>
		<li value="1">${l.l('Принудительный')}</li>
	</jsp:attribute>
</ui:combo-single>

<ui:button styleClass="ml1" type="run" onclick="
	if (!confirm(this.form.confirmText.value)) return;
	$$.ajax.post(this, {failAlert: false})
		.fail(() => $$.shell.message.show('${l.l('Restart')}', '${l.l('Restart has been done, refresh the browser site')}'));
	"/>
