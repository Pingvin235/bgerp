<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="id" value="${form.param.id}"/>
<c:set var="log" value="${form.response.data.log}"/>

<c:set var="parameterGroup" value="${form.response.data.parameterGroup}"/>

<c:set var="paramLinkId" value="${u:uiid()}"></c:set>

<div class="center1020" id="${paramLinkId}">
	<button class="btn-white mb1" onclick="openUrlToParent( '${form.returnUrl}', $('#${paramLinkId}') );">Закрыть</button>
	<table style="width:100%" class="data">
		<tr ${hideTr}>
			<td>Дата</td>
			<td>Пользователь</td>
			<td width="100%">Текст</td>
		</tr>	
		<c:forEach var="logItem" items="${log}">
			<tr>
				<td nowrap="nowrap">${logItem.getDateFormatted()}</td>
				<td nowrap="nowrap">${ctxUserMap[logItem.userId].title}</td>			
				<td width="100%">${logItem.text}</td>
			</tr>
		</c:forEach>
	</table>
	 
	<c:set var="state" value="${l.l('Лог изменений')}"/>
	<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
</div>




