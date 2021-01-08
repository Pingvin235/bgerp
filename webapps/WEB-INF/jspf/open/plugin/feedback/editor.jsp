<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<html:form styleId="${uiid}" action="/open/plugin/feedback/message" style="width: 50%;">
	<html:hidden property="action" value="add"/>
	<html:hidden property="processId"/>

	<h1>${l.l('Написать сообщение')}</h1>
	<div class="in-inline-block w100p">
		<div style="width: 50%;">
			<h2>${l.l('Тема')} *</h2>
			<input type="text" name="subject" class="w100p"/>
		</div><%--
	--%><div class="pl1" style="width: 50%;">
			<h2>E-Mail *</h2>
			<input type="text" name="email" class="w100p"/>
		</div>
	</div>
	<div>
		<h2>${l.l('Текст')} *</h2>
		<textarea name="text" class="w100p" rows="20"/>
	</div>
	<div class="pt1">
		<c:set var="returnCommand">$$.ajax.load('/open/process.do?action=show&id=${form.param.processId}', $$.shell.$content())</c:set>
		<button type="button" class="btn-grey"
			onclick="$$.ajax.post(this.form).done(() => ${returnCommand})"
		>OK</button>
		<button type="button" class="btn-grey ml1" onclick="${returnCommand}">${l.l('Отмена')}</button>
	</div>
</html:form>