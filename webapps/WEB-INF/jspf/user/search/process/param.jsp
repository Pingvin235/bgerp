<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="common.jsp"%>

<table class="data mt1">
	<tr>
		<td width="30">ID</td>
		<td>${l.l('Title')}</td>
		<td>${l.l('Параметр')}</td>
		<td>${l.l('Value')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td><ui:process-link process="${item.object}"/></td>
			<td>${item.object.description}</td>
			<td>${item.param.title}</td>
			<td>${item.value}</td>
		</tr>
	</c:forEach>
</table>