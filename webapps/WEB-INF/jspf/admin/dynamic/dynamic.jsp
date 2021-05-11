<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="runFormUiid" value="${u:uiid()}"/>

<html:form action="admin/dynamic" onsubmit="return false;" style="display: inline-block;">
	<input type="hidden" name="action" value="recompileAll"/>
	<input type="hidden" name="class"/>

	<button class="btn-grey" type="button" onclick="
		this.form.class.value = $('#${runFormUiid}')[0].class.value;
		$$.ajax.load(this.form, $$.shell.$content(this));
	">${l.l('Скомпилировать всё')}</button>
</html:form>
<html:form action="admin/dynamic" onsubmit="return false;" styleId="${runFormUiid}" style="display: inline-block;">
	<input type="hidden" name="action" value="runDynamicClass"/>
	<input type="hidden" name="iface" value="runnable"/>
	
	<input type="text" name="class" value="${form.param['class']}" class="ml2" size="50" placeholder="${l.l('Имя класса, реализующего java.lang.Runnable')}"/>
	<input type="checkbox" name="sync" value="true" title="${l.l('Ожидание окончания выполнения')}"/>&nbsp;${l.l('синхронно')}
	
	<button class="btn-grey ml1" type="button" onclick="
		$$.ajax.post(this.form)
			.done(() => {
				alert(this.form.sync.checked ? '${l.l('Класс выполнен, проверьте логи')}' : '${l.l('Класс запущен в отдельном потоке,\\nвывод в логах.')}')
			})">${l.l('Выполнить')}</button>
</html:form>

<c:set var="result" value="${form.response.data.result}"/>

<c:if test="${not empty result}">
	<div class="mt1">
		<b>Результат компиляции:</b><br/><br/>
		${u:htmlEncode(result.logString)}
	</div>
</c:if>

<shell:title ltext="Динамический код"/>
<shell:state help="kernel/extension.html#dyn"/>
