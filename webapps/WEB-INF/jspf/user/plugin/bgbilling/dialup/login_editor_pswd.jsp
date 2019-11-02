<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="enable"><p:check action="'ru.bgcrm.plugin.bgbilling.proto.struts.action.DialUpAction:updateLoginPassword">enable</p:check></c:set>

<html:form action="/user/plugin/bgbilling/proto/dialup.do" styleClass="${enable}">
	<input type="hidden" name="action" value="updateLoginPassword" />
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>
	
	<h2>Пароль</h2>
	
	<table class="data" style="width: 100%;">
		<tr>
			<td>Дата и время</td>
			<td>Исполнитель</td>
		</tr>
		<c:forEach var="item" items="${form.response.data.pswdLog}">
			<tr>
				<td nowrap="nowrap">${u:formatDate( item.time, 'ymdhms' )}</td>
				<td>${item.user}</td>												
			</tr>
		</c:forEach>	
	</table>
</html:form>