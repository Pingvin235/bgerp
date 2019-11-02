<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data">
	<tr>
		<td>ID</td>
		<td>Номер</td>
	</tr>
		
	<c:forEach var="item" items="${form.response.data.list}">
	<tr>
		<td>${item.id}</td>
		<td><a href="#UNDEF" onclick="bgbilling_openCommonContract( ${item.id} ); return false;">${item.title}</a></td>
	</tr>
	</c:forEach>
</table>