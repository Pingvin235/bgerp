<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="height: 100%; display: flex; flex-flow: column;">
	<html:form action="user/log" onsubmit="return false;" style="flex: 0 1 auto;">
		<input type="hidden" name="action" value="log"/>
		<input type="hidden" name="enable"/>
		<button class="${form.response.data.state ? 'btn-blue' : 'btn-white'}" type="button" onclick="this.form.enable.value=1; openUrlContent(formUrl(this.form));">${l.l('Включить')}</button>
		<button class="ml1 ${form.response.data.state ? 'btn-white' : 'btn-blue'}" type="button" onclick="this.form.enable.value=0; openUrlContent(formUrl(this.form));">${l.l('Выключить')}</button>
	</html:form>
	<h2>${l.l('Текущий лог')}:</h2>
	<div style="flex: 1;">
		<textarea style="width: 100%; height: 100%;" wrap="off"> ${form.response.data.log}</textarea>
	</div>
</div>

<c:set var="title" value="${l.l('Логирование')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/extension.html#log-dyn"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>

<script>
	$(function () {
		$('#content > #log').data('onShow', function () {
			$$.ajax.load("/user/log.do", $$.shell.$content());
		});
	});
</script>