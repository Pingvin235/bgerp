<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="admin/user">
	<input type="hidden" name="action" value="groupList"/>
	
	<table>
		<tr>
			<td width="100%">
				 <c:url var="url" value="customer.do">
				    <c:param name="action" value="groupGet"/>
				    <c:param name="id" value="-1"/>
				    <c:param name="returnUrl" value="${form.requestUrl}"/>
			  	</c:url>
			  	<input type="button" value="Создать" onclick="openUrlContent'${url}' )"/>
			</td>
			<%--
			<td>
				<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
			</td>
			--%>
		</tr>
	</table>
</html:form>

<table style="width: 100%;" class="data">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="50%">Название</td>
		<td width="50%">Комментарий</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="customer.do">
				<c:param name="action" value="groupGet"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			
			<c:url var="deleteAjaxUrl" value="customer.do">
				<c:param name="action" value="groupDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>
			
			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
			<td align="right">${item.id}</td>
			<td>${item.title}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>