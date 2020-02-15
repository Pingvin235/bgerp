<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="config" value="${u:getConfig(ctxSetup, 'ru.bgcrm.plugin.task.Config')}"/> 

<table style="width: 100%;" class="data mt05">
	<tr>
		<td width="30">ID</td>
		<td>Тип</td>
		<td>Запланированное время</td>
		<td>Выполнена</td>
		<td width="30">&#160;</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}" >
		<tr>
			<td>${item.id}</td>
			<td>${config.getType(item.typeId).title}</td>
			<td>${u:formatDate(item.scheduledTime, 'ymdhms')}</td>
			<td>${u:formatDate(item.executedTime, 'ymdhms')}</td>
			<td nowrap="nowrap"></td>
		</tr>
	</c:forEach>	
</table>
