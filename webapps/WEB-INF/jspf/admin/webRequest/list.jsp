<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>


<c:set var="pageFormId" value="webRequestSearchForm" scope="request"/>
<c:set var="nextCommand" value="; openUrlTo(formUrl($('#webRequestSearchForm')),$('#webRequestSearchForm-requestList'));" scope="request"/>
<%@ include file="/WEB-INF/jspf/page_control.jsp"%>

<div>
	<table style="width: 100%;" class="data">
		<tr>
			<td align="center">ID</td>
			<td>Время</td>
			<td>Ip адрес</td>	
			<td>Пользователь</td>
			<td>Действие</td>
			<td>Параметры запроса</td>
			<td>Продолжительность</td>
		</tr>
		<c:forEach var="item" items="${form.response.data.list}">
			<tr>
				<td nowrap="nowrap" align="center">${item.id}</td>
				<td nowrap="nowrap" align="center">
					<fmt:formatDate value="${item.time}" var="time" pattern="dd.MM.yyyy HH:mm:ss"/>
					${time}
				</td>
				<td nowrap="nowrap" align="center">${item.ipAddress}</td>
				<td>${ctxUserMap[item.uid].title}</td>
				<td>${item.action}</td>
				<td>
					<c:set var="maxLength" value="150"/>
					<c:set var="text" value="${item.parameters}"/>
					<%@include file="/WEB-INF/jspf/short_text.jsp"%>
				</td>
				<td>${item.duration}</td>
			</tr>
		</c:forEach>
	</table>
</div>
