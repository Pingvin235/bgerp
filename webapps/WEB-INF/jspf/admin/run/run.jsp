<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<p:check action="org.bgerp.action.admin.RunAction:runClass">
	<c:set var="runFormUiid" value="${u:uiid()}"/>
	<html:form action="admin/run" onsubmit="return false;" styleId="${runFormUiid}" style="display: inline-block;">
		<input type="hidden" name="action" value="runClass"/>
		<input type="hidden" name="iface" value="runnable"/>
		
		<input type="text" name="class" value="${form.param['class']}" size="50" placeholder="${l.l('Имя класса, реализующего java.lang.Runnable')}"/>
		
		<ui:combo-single
			hiddenName="sync" value="0" prefixText="${l.l('Ожидание окончания выполнения')}:" styleClass="ml1">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('Нет')}</li>
				<li value="1">${l.l('Да')}</li>
			</jsp:attribute>
		</ui:combo-single>

		<button class="btn-grey ml1 icon" type="button" onclick="
			$$.ajax.post(this.form, {control: this})
				.done(() => {
					alert(this.form.sync.checked ? '${l.l('Класс выполнен, проверьте логи')}' : '${l.l('Класс запущен в отдельном потоке,\\nвывод в логах.')}')
				})"><i class="ti-control-play"></i></button>
	</html:form>
</p:check>

<shell:title ltext="Выполнить"/>
