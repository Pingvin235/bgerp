<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="search_common.jsp"%>

<table class="data mt1 hl">
	<tr>
		<td width="30">ID</td>
		<td>${l.l('Process')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td>${item.id}</td>
			<td><ui:process-link process="${item}"/></td>
		</tr>
	</c:forEach>
</table>