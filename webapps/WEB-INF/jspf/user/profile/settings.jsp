<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="${form.requestURI}">
	<input type="hidden" name="method" value="updateSettings"/>

	<table class="data">
		<tr>
			<td>${l.l('Свойство')}</td>
			<td width="100%">${l.l('Value')}</td>
		</tr>
		<tr>
			<td>${l.l('Name')}</td>
			<td>
				<input name="userName" type="text" size="30" value="${ctxUser.title}"/>
			</td>
		</tr>
		<tr>
			<td>${l.l('Login')}</td>
			<td><input name="userLogin" type="text" size="30" value="${ctxUser.login}"/></td>
		</tr>
		<tr>
			<td>${l.l('Password')}</td>
			<td><input name="userPassword" type="password" size="30" value="${ctxUser.password}"/></td>
		</tr>
		<tr>
			<td>${l.l('Comment')}</td>
			<td><input name="userDescription" type="text" size="30" value="${ctxUser.comment}"/></td>
		</tr>
	</table>

	<c:url var="url" value="${form.requestURI}">
		<c:param name="userId" value="${ctxUser.id}"/>
	</c:url>
	<button class="btn-grey mt1" type="button" onclick="$$.ajax.post(this).done($$.ajax.loadContent('${url}', this))">${l.l('Сохранить свойства')}</button>
</html:form>