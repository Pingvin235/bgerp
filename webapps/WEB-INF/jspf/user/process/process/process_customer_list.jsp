<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table style="width:100%;" class="data">
<tr>
	<td>ID</td>
	<td>Контрагент</td>
</tr>
	<c:forEach var="link" items="${form.response.data.list}">
	<tr>
		<td><a href="#" onclick="openCustomer( ${link.id} ); return false;">${link.id}</a></td>
		<td><a href="#" onclick="openCustomer( ${link.id} ); return false;">${link.title}</a></td>
	<tr>
	</c:forEach>
</table>