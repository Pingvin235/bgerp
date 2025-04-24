<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="search_common.jsp"%>

<c:set var="config" value="${ctxSetup.getConfig('MessageTypeConfig')}"/>

<table class="data mt1">
	<tr>
		<td width="30">ID</td>
		<td>${l.l('Type')}</td>
		<td>${l.l('Time')}</td>
		<td>${l.l('Process')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<c:set var="message" value="${item.first}"/>
		<c:set var="messageType" value="${config.typeMap[message.typeId]}"/>
		<c:set var="process" value="${item.second}"/>
		<tr>
			<td>${message.id}</td>
			<td>
				<%-- <%@ include file="/WEB-INF/jspf/user/message/message_direction.jsp"%>&nbsp; --%>
				${messageType.title}
			</td>
			<td nowrap="nowrap">${tu.format(message.fromTime, 'ymdhm')}</td>
			<td>
				<c:if test="${not empty process}">
					<ui:process-link process="${process}"/>
				</c:if>
			</td>
		</tr>
	</c:forEach>
</table>