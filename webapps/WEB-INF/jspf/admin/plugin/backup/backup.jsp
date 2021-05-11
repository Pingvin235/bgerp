<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>${l.l('Создать Backup')}</h2>
<html:form action="admin/plugin/backup/backup">
	<input type="hidden" name="action" value="backup"/>
	<ui:combo-single hiddenName="db" widthTextValue="2em" prefixText="${l.l('Включить БД')}:">
		<jsp:attribute name="valuesHtml">
			<li value="0">${l.l('Нет')}</li>
			<li value="1">${l.l('Да')}</li>
		</jsp:attribute>
	</ui:combo-single>
	<ui:button type="run" styleClass="ml1" onclick="
		$$.ajax.post(this.form, {control: this}).done(() => {
			$$.ajax.load('${form.requestUrl}', $$.shell.$content(this));
		});"/>
</html:form>

<h2>${l.l('Файлы')}</h2>
<ui:files files="<%=org.bgerp.plugin.svc.backup.action.BackupAction.FILE_BACKUP%>"/>

<shell:title text="Backup"/>
