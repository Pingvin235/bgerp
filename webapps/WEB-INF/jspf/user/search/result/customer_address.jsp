<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="search_common.jsp"%>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">ID</td>
		<td>${l.l('Наименование')}</td>
		<td>${l.l('Параметр')}</td>
		<td>${l.l('Значение')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td>${item.object.id}</td>
			<td><a href="#" onclick="openCustomer( ${item.object.id} ); return false;">${item.object.title}</a></td>			
			<td>${item.param.title}</td>
			<td>${item.value}</td>
		</tr>
	</c:forEach>
</table>