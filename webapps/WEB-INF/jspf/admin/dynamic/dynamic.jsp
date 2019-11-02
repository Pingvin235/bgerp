<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
		
<html:form action="admin/dynamic" onsubmit="return false;" style="display: inline-block;">
	<input type="hidden" name="action" value="recompileAll"/>
	
	<button class="btn-grey" type="button" onclick="openUrlContent(formUrl(this.form ));">Скомпилировать всё</button>
</html:form>
<html:form 	action="admin/dynamic" onsubmit="return false;" style="display: inline-block;">
	<input type="hidden" name="action" value="runDynamicClass"/>
	<input type="hidden" name="iface" value="runnable"/>
	
	<input type="text" name="class" class="ml2" size="50" placeholder="Имя класса, реализующего java.lang.Runnable"/>
	<input type="checkbox" name="sync" value="true" title="Ожидание окончания выполнения"/>&nbsp;${l.l('синхронно')}
	
	<button class="btn-grey ml1" type="button" onclick="
		this.disabled = true;
		bgerp.ajax.post(formUrl(this.form)).done(() => {
			this.disabled = false;
			alert(this.form.sync.checked ? 'Класс выполнен, проверьте логи' : 'Класс запущен в отдельном потоке,\nвывод в логах.')
		})">Выполнить</button>
</html:form>

<c:set var="result" value="${form.response.data.result}"/>

<c:if test="${not empty result}">
	<div class="mt1">
		<b>Результат компиляции:</b><br/><br/>
		${u:htmlEncode(result.logString)}
	</div>
</c:if>

<c:set var="title" value="Динамический код"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/extension.html#dyn"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
