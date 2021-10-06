<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="logged" value="${form.response.data.logged}"/>

<div class="center1020">
	<h2>${l.l('Авторизовавшиеся пользователи')} [ ${logged.size()} ]</h2>
	
	<table class="data" style="width: 100%;">
		<tr>
			<td>${l.l('Пользователь')}</td>
			<td>${l.l('Сессии (вход / последняя активность)')}</td>
		</tr>
		<c:forEach var="item" items="${logged}">
			<c:set var="sessions" value="${item.value}"/>
			<tr>
				<td><ui:user-link id="${item.key.id}"/></td>
				<td>
					<c:forEach var="session" items="${sessions}">
						${tu.format( session.loginTime, 'ymdhms' )} /
						${tu.format( session.lastActiveTime, 'ymdhms' )}<br/>
					</c:forEach>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<c:set var="title" value="${l.l('Авторизовавшиеся пользователи')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>