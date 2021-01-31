<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="admin/custom" onsubmit="return false;" style="display: inline-block;">
	<input type="hidden" name="action" value="compile"/>
	<button class="btn-grey" type="button" onclick="$$.ajax.load(this.form, $$.shell.$content());">${l.l('Скомпилировать всё')}</button>
</html:form>

<c:set var="result" value="${form.response.data.result}"/>

<c:if test="${not empty result}">
	<div class="mt1">
		<b>${l.l('Результат компиляции')}:</b><br/><br/>
		${u:htmlEncode(result.logString)}
	</div>
</c:if>

<shell:title ltext="Кастомизация"/>
<shell:state help="kernel/extension.html#custom"/>
