<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data mt1 hl">
	<tr>
		<td>&nbsp;</td>
		<td width="100%">${l.l('Title')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td>
				<input type="checkbox" name="processId" value="${item.id}"/>
			</td>
			<td>
				<%-- large descriptions are not cut --%>
				<ui:process-link id="${item.id}"  text="${item.title}"/>
			</td>
		</tr>
	</c:forEach>
</table>