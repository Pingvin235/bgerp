<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<h2>${l.l('Status')}</h2>
	<pre>${form.response.data.status}</pre>

	<h2>${l.l('Перезапуск')}</h2>
	<%@ include file="app_restart.jsp"%>

	<h2>${l.l('Логи приложения')}</h2>
	<ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_APP%>" requestUrl="${form.requestUrl}"/>

	<h2>${l.l('Access logs')}</h2>
	<ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_ACCESS%>" requestUrl="${form.requestUrl}" maxCount="10"/>

	<p:check action="org.bgerp.action.admin.AppAction:update">
		<h2>${l.l('Обновление')}</h2>
		<html:form action="/admin/app">
			<input type="hidden" name="action" value="update"/>
			<ui:combo-single hiddenName="force" widthTextValue="3em" prefixText="${l.l('Принудительно')}:">
				<jsp:attribute name="valuesHtml">
					<li value="0">${l.l('No')}</li>
					<li value="1">${l.l('Yes')}</li>
				</jsp:attribute>
			</ui:combo-single>
			<%@ include file="run_restart_button.jsp"%>
		</html:form>
	</p:check>

	<p:check action="org.bgerp.action.admin.AppAction:updateToChange">
		<h2>${l.l('Обновление на изменение')}</h2>
		<html:form action="/admin/app">
			<input type="hidden" name="action" value="updateToChange"/>
			<ui:combo-single hiddenName="changeId" widthTextValue="12em" prefixText="ID:">
				<jsp:attribute name="valuesHtml">
					<c:forEach var="item" items="${form.response.data.changes}">
						<li value="${item.id}">${item.title}</li>
					</c:forEach>
				</jsp:attribute>
			</ui:combo-single>
			<%@ include file="run_restart_button.jsp"%>
		</html:form>
	</p:check>

	<h2>${l.l('Логи обновлений')}</h2>
	<ui:files files="<%=org.bgerp.action.admin.AppAction.LOG_UPDATE%>" requestUrl="${form.requestUrl}" maxCount="20"/>

	<h2>${l.l('Файлы обновлений')}</h2>
	<ui:files files="<%=org.bgerp.action.admin.AppAction.UPDATE_ZIP%>" requestUrl="${form.requestUrl}" maxCount="20"/>
</div>

<shell:title ltext="Статус приложения"/>
<shell:state error="${form.response.data.error}"/>