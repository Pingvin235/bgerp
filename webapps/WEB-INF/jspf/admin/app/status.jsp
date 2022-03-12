<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<h2>${l.l('Статус')}</h2>
	<pre>${form.response.data.status}</pre>

	<%@ include file="app_restart.jsp"%>

	<h2>${l.l('Логи приложения')}</h2>
	<ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_APP%>" maxCount="20"/>

	<p:check action="org.bgerp.action.admin.AppAction:update">
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
			<%@ include file="run_restart_button.jsp"%>
		</html:form>
	</p:check>

	<p:check action="org.bgerp.action.admin.AppAction:updateToChange">
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
			<%@ include file="run_restart_button.jsp"%>
		</html:form>
	</p:check>

	<h2>${l.l('Логи обновлений')}</h2>
	<ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_UPDATE%>" maxCount="20"/>
</div>
</div>

<shell:title ltext="Статус приложения"/>
<shell:state text=""/>