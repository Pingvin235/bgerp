<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>SRC</h2>
<ui:files files="<%=org.bgerp.action.admin.CustomAction.CUSTOM_SRC%>" maxCount="20"/>

<html:form action="/admin/custom" onsubmit="return false;" style="display: inline-block;" styleClass="mt1">
	<input type="hidden" name="action" value="compile"/>
	<button class="btn-grey" type="button" onclick="$$.ajax.load(this.form, $$.shell.$content(this));">${l.l('Скомпилировать Java код')}</button>
</html:form>

<c:set var="result" value="${form.response.data.result}"/>
<c:if test="${not empty result}">
	<div class="mt1">
		<b>${l.l('Результат компиляции')}:</b><br/>
		${u:htmlEncode(result.logString)}
	</div>
</c:if>

<h2>JAR</h2>
<ui:files files="<%=org.bgerp.action.admin.CustomAction.CUSTOM_JAR%>" requestUrl="${form.requestUrl}" maxCount="1"/>

<%-- <p:check action="ru.bgcrm.struts.action.admin.AppAction:restart">
	<button class="mt1 btn-grey" type="button" onclick="$$.ajax.post('/admin/app.do?action=restart', {control: this})">${l.l('Перезапустить')}</button>
</p:check> --%>

<%@ include file="../app/app_restart.jsp"%>

<shell:title ltext="Custom"/>
<shell:state help="kernel/extension.html#custom"/>
