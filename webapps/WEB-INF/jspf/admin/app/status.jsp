<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<h2>${l.l('Статус')}</h2>
	<pre>${form.response.data.status}</pre>

	<h2>${l.l('Перезапуск')}</h2>
	<html:form action="admin/app">
		<input type="hidden" name="action" value="restart"/>
		<ui:combo-single hiddenName="force" widthTextValue="5em" prefixText="${l.l('Перезапуск')}:">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('Нормальный')}</li>
				<li value="1">${l.l('Принудительный')}</li>
			</jsp:attribute>
		</ui:combo-single>
		<button class="btn-grey ml1" type="button" onclick="
			this.disabled = true;
			$$.ajax.post(this.form).always(() => {
				this.disabled = false;
			});">${l.l('Перезапустить')}</button>
	</html:form>
	
	<h2>${l.l('Логи приложения')}</h2>
	<ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_APP%>" maxCount="20"/>

	<h2>${l.l('Обновление')}</h2>
	<html:form action="admin/app">
		<input type="hidden" name="action" value="update"/>
		<ui:combo-single hiddenName="force" widthTextValue="3em" prefixText="${l.l('Принудительно')}:">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('Нет')}</li>
				<li value="1">${l.l('Да')}</li>
			</jsp:attribute>
		</ui:combo-single>
		<ui:combo-single hiddenName="restartForce" widthTextValue="5em" prefixText="${l.l('Перезапуск')}:" styleClass="ml05">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('Нормальный')}</li>
				<li value="1">${l.l('Принудительный')}</li>
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
		<ui:combo-single hiddenName="changeId" widthTextValue="5em" prefixText="ID:">
			<jsp:attribute name="valuesHtml">
				<c:forEach var="item" items="${form.response.data.changeIds}">
					<li value="${item}">${item}</li>
				</c:forEach>
			</jsp:attribute>
		</ui:combo-single>
		<ui:combo-single hiddenName="restartForce" widthTextValue="5em" prefixText="${l.l('Перезапуск')}:" styleClass="ml05">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('Нормальный')}</li>
				<li value="1">${l.l('Принудительный')}</li>
			</jsp:attribute>
		</ui:combo-single>

		<button class="btn-grey ml1" type="button" onclick="
			this.disabled = true;
			$$.ajax.post(this.form).always(() => {
				this.disabled = false;
			});">${l.l('Обновить')}</button>
	</html:form>

	<h2>${l.l('Логи обновлений')}</h2>
	<ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_UPDATE%>" maxCount="20"/>
</div>
</div>

<c:set var="title" value="${l.l('Статус приложения')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>