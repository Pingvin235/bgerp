<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="group" value="${form.response.data.group}"/>
	
<html:form action="admin/customer" onsubmit="return false;">
	<input type="hidden" name="action" value="groupUpdate"/>
	<html:hidden property="id"/>

	<table style="width: 100%;" class="data">
		<tr>
			<td width="100">Параметр</td>
			<td>Значение</td>
		</tr>
		<tr>
			<td>ID</td>
			<td>${group.id}</td>
		</tr>
		<tr>
			<td>Название</td>
			<td><html:text property="title" style="width: 100%" value="${group.title}"/></td>
		</tr>
		<tr>
			<td>Комментарий</td>
			<td><html:textarea property="comment" style="width: 100%" value="${group.comment}" /></td>
		</tr>
		<tr>
			<td colspan="2">
				<%@ include file="/WEB-INF/jspf/send_and_cancel_form.jsp"%>
			</td>
		</tr>
	</table>
</html:form>