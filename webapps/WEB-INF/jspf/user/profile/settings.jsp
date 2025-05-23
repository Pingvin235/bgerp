<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="${form.requestURI}">
	<input type="hidden" name="method" value="updateSettings"/>

	<c:set var="user" value="${frd.user}" />

	<c:url var="url" value="${form.requestURI}">
		<c:param name="method" value="getUserProfile"/>
		<c:param name="userId" value="${user.id}"/>
	</c:url>

	<table class="data">
		<tr>
			<td>${l.l('Свойство')}</td>
			<td width="100%">${l.l('Value')}</td>
		</tr>
		<tr>
			<td>${l.l('Name')}</td>
			<td>
				<input id="userName" name="userName" type="text" size="30" value="${user.title}"/>
			</td>
		</tr>
		<tr>
			<td>${l.l('Логин')}</td>
			<td><input id="userLogin" name="userLogin" type="text" size="30" value="${user.login}"/></td>
		</tr>
		<tr>
			<td>${l.l('Пароль')}</td>
			<td><input id="userPassword" name="userPassword" type="password" size="30" value="${user.password}"/></td>
		</tr>
		<tr>
			<td>${l.l('Comment')}</td>
			<td><input id="userDescription" name="userDescription" type="text" size="30" value="${user.description}"/></td>
		</tr>
	</table>

	<button class="btn-grey mt1" type="button" onclick="$$.ajax.post(this).done($$.ajax.loadContent('${url}', this))">${l.l('Сохранить свойства')}</button>
</html:form>