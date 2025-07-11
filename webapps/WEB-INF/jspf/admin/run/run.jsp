<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<p:check action="/admin/run:runClass">
	<c:set var="runFormUiid" value="${u:uiid()}"/>
	<html:form action="/admin/run" onsubmit="return false;" styleId="${runFormUiid}" style="display: inline-block;">
		<input type="hidden" name="method" value="runClass"/>
		<input type="hidden" name="iface" value="runnable"/>

		<ui:select-single hiddenName="class" list="${runnableClasses}" style="width: 30em;" placeholder="${l.l('Имя класса, реализующего java.lang.Runnable')}"/>

		<ui:combo-single
			hiddenName="sync" value="0" prefixText="${l.l('Wait of execution is done')}:" styleClass="ml1">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('No')}</li>
				<li value="1">${l.l('Yes')}</li>
			</jsp:attribute>
		</ui:combo-single>

		<button class="btn-grey ml1 icon" type="button" onclick="
			$$.ajax.post(this)
				.done(() => {
					alert(this.form.sync.value ? '${l.l('Класс выполнен, проверьте логи')}' : '${l.l('Класс запущен в отдельном потоке,\\nвывод в логах.')}')
				})"><i class="ti-control-play"></i></button>
	</html:form>
</p:check>

<shell:title text="${l.l('Run')}"/>
<shell:state help="kernel/setup.html#run"/>