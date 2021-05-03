<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="admin/custom" onsubmit="return false;" style="display: inline-block;">
	<input type="hidden" name="action" value="compile"/>
	<button class="btn-grey" type="button" onclick="$$.ajax.load(this.form, $$.shell.$content());">${l.l('Скомпилировать всё')}</button>
</html:form>

<c:set var="result" value="${form.response.data.result}"/>

<c:if test="${not empty result}">
	<div class="mt1">
		<b>${l.l('Результат компиляции')}:</b><br/>
		${u:htmlEncode(result.logString)}
	</div>
	<p:check action="ru.bgcrm.struts.action.admin.AppAction:restart">
		<c:if test="${result.result}">
			<button class="mt1 btn-grey" type="button" onclick="$$.ajax.post('/admin/app.do?action=restart', {control: this})">${l.l('Перезапустить')}</button>
		</c:if>
	</p:check>
</c:if>

<shell:title ltext="Custom"/>
<shell:state help="kernel/extension.html#custom"/>
