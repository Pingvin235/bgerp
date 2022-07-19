<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/plugin/slack/config" styleClass="center1020">
	<input type="hidden" name="action" value="token"/>

	<h2>Client ID</h2>
	<div>
		<input type="text" name="clientId" value="${form.param.clientId}" size="40" placeholder="${l.l('Пример')}: 112244252083.2278905589302"/>
		<%-- https://api.slack.com/authentication/oauth-v2#exchanging --%>
		<c:url var="url" value="https://slack.com/oauth/v2/authorize">
			<%-- https://api.slack.com/scopes --%>
			<c:param name="user_scope"
				value="channels:history,channels:read,channels:write,groups:read,groups:write,groups:history,chat:write,users:read"/>
		</c:url>

		<button type="button" class="btn-white ml1"
			title="${l.l('Скопируйте из URL после авторизации параметр code')}"
			onclick="window.open('${url}&client_id=' + this.form.clientId.value, '_blank')"
		>${l.l('Получить Authorization code')}</button>
	</div>

	<h2>Authorization Code</h2>
	<input type="text" name="authCode" value="${form.param.authCode}" class="w100p" placeholder="${l.l('Пример')}: 112244252083.2315102856660.afac8bee56080b14aa2f908c94708a9a976335167548c1aaf2a4c16e74b363da"/>

	<h2>Client Secret</h2>
	<div>
		<html:password property="clientSecret" size="40"/>
		<button type="button" class="btn-white ml1" onclick="$$.ajax.loadContent(this);">${l.l('Получить Token')}</button>
	</div>

	<c:set var="log" value="${form.response.data.log}"/>
	<c:if test="${not empty log}">
		<h2>Log</h2>
		<pre>${log}</pre>
	</c:if>
</html:form>

<shell:title ltext="Slack Конфигурация"/>
