<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<h2>${l.l('Статус')}</h2>
	<pre>${form.response.data.status}</pre>
	
	<h2>${l.l('Обновление')}</h2>
	<html:form action="admin/app">
		<input type="hidden" name="action" value="update"/>
		<ui:combo-single hiddenName="force" prefixText="${l.l('Принудительно')}:">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('Нет')}</li>
				<li value="1">${l.l('Да')}</li>
			</jsp:attribute>
		</ui:combo-single>
		<button class="btn-grey ml1" type="button" onclick="
			this.disabled = true;
			$$.ajax.post(this.form).always(() => {
				this.disabled = false;
			});">${l.l('Обновить')}</button>
	</html:form>

	<h2>${l.l('Обновление на изменение')}</h2>
	<html:form action="admin/app">
		<input type="hidden" name="action" value="updateToChange"/>
		<input type="text" name="processId" size="10" style="text-align: center;" placeholder="${l.l('ID процесса')}"/>
		<button class="btn-grey ml1" type="button" onclick="
			this.disabled = true;
			$$.ajax.post(this.form).always(() => {
				this.disabled = false;
			});">${l.l('Обновить')}</button>
	</html:form>

	<h2>${l.l('Логи обновлений')}</h2>
	<table class="data" style="width: 100%;">
		<tr>
			<td>${l.l('Файл')}</td>
			<td>${l.l('Размер')}</td>
		</tr>
		<c:forEach var="file" items="${form.response.data.logUpdateList}">
			<tr>
				<td>${file.name}</td>
				<td>${file.length()}</td>
			</tr>
		</c:forEach>
	</table>
</div>
</div>

<c:set var="title" value="${l.l('Статус приложения')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>