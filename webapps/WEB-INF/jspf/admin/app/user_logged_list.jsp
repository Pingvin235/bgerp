<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="logged" value="${form.response.data.logged}"/>

<div class="center1020">
	<h2>${l.l('Авторизовавшиеся пользователи')} [ ${logged.size()} ]</h2>

	<table class="data">
		<tr>
			<td>${l.l('User')}</td>
			<td>${l.l('Время входа')}</td>
			<td>${l.l('Последняя активность')}</td>
			<td>IP</td>
		</tr>
		<c:forEach var="item" items="${logged}">
			<c:set var="sessions" value="${item.value}"/>
			<c:forEach var="session" items="${sessions}">
				<tr>
					<td><ui:user-link id="${item.key.id}"/></td>
					<td>${tu.format(session.loginTime, 'ymdhms')}</td>
					<td>${tu.format(session.lastActiveTime, 'ymdhms')}</td>
					<td>${session.ip}</td>
				</tr>
			</c:forEach>
		</c:forEach>
	</table>
</div>

<c:set var="title" value="${l.l('Авторизовавшиеся пользователи')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>