<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<p:check action="org.bgerp.action.admin.RunAction:runClass">
	<c:set var="runFormUiid" value="${u:uiid()}"/>
	<html:form action="/admin/run" onsubmit="return false;" styleId="${runFormUiid}" style="display: inline-block;">
		<input type="hidden" name="action" value="runClass"/>
		<input type="hidden" name="iface" value="runnable"/>

		<ui:select-single hiddenName="class" list="${taskClasses}" style="width: 30em;" placeholder="${l.l('Имя класса, реализующего java.lang.Runnable')}"/>

		<ui:combo-single
			hiddenName="sync" value="0" prefixText="${l.l('Ожидание окончания выполнения')}:" styleClass="ml1">
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

<%-- TODO: Table with configured Scheduler Tasks.
<p:check action="org.bgerp.action.admin.RunAction:scheduler">
	<c:url var="url" value="/user/admin/run.do">
		<c:param name="action" value="scheduler"/>
	</c:url>
	<c:import url="${url}"/>
</p:check> --%>

<shell:title ltext="Выполнить"/>
