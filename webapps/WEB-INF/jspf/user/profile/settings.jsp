<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form method="GET" action="/user/profile.do" style="width: 100%;">
	<input type="hidden" name="action" value="updateSettings"/>
	
	<c:url var="url" value="profile.do">
		<c:param name="action" value="getUserProfile" />
	</c:url>

	<c:set var="user" value="${form.response.data.user}" />

	<table style="width: 100%;" class="data">
		<tr>
			<td>${l.l('Свойство')}</td>
			<td width="100%">${l.l('Значение')}</td>
		</tr>
		<tr>
			<td>${l.l('Имя')}</td>
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
			<td>${l.l('Комментарий')}</td>		
			<td><input id="userDescription" name="userDescription" type="text" size="30" value="${user.description}"/></td>
		</tr>
		<%--
		<tr>
			<td nowrap="nowrap">Стиль интерфейса<br/>(применяется после повторного входа)</td>		
			<td>
				<html:select property="theme" value="${ctxUser.personalizationMap['ui-theme'] }">
					<html:option value="ui-start">ui-start</html:option> 
					<html:option value="ui-lightness">ui-lightness</html:option>
					<html:option value="ui-redmond">ui-redmond</html:option>
					<html:option value="ui-smoothness">ui-smoothness</html:option>
				</html:select>
			</td>
		</tr>
		--%>		
	</table>
	
	<button class="btn-grey mt1" type="button" onclick="bgerp.ajax.post(formUrl(this.form)).done(openUrlContent('${url}'))">${l.l('Сохранить свойства')}</button>
</html:form>