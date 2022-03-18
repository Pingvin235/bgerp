<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<p:check action="org.bgerp.action.admin.AppAction:restart">
	<h2>${l.l('Перезапуск')}</h2>
	<html:form action="/admin/app">
		<input type="hidden" name="action" value="restart"/>
		<ui:combo-single hiddenName="force" widthTextValue="5em" prefixText="${l.l('Перезапуск')}:">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('Нормальный')}</li>
				<li value="1">${l.l('Принудительный')}</li>
			</jsp:attribute>
		</ui:combo-single>
		<%@ include file="run_restart_button.jsp"%>
	</html:form>
</p:check>