<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="lic" value="${form.response.data.lic}"/>
<c:set var="status" value="${form.response.data.status}"/>
<c:set var="logged" value="${form.response.data.logged}"/>

<div class="center1020">
	<h2>Статус сервера</h2>
	<pre>${status}</pre>	
	
	<h2>Авторизовавшиеся пользователи [ ${logged.size()} ]</h2>
	
	<table class="data" style="width: 100%;">
		<tr>
			<td>Пользователь</td>
			<td>Сессии (вход / последняя активность)</td>
		</tr>
		<c:forEach var="item" items="${logged}">
			<c:set var="userId" value="${item.key.id}"/>
			<c:set var="sessions" value="${item.value}"/>
			
			<tr>
				<td><%@ include file="/WEB-INF/jspf/user_link.jsp"%></td>
				<td>
					<c:forEach var="session" items="${sessions}">
						${u:formatDate( session.loginTime, 'ymdhms' )} /
						${u:formatDate( session.lastActiveTime, 'ymdhms' )}<br/>
					</c:forEach>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<c:set var="title" value="Статус сервера"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>