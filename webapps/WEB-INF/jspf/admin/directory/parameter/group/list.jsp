<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<%@ include file="../../create_button.jsp"%>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="100%">${l.l('Название')}</td>
	</tr>
	<c:forEach var="item" items="${parameterList}">
		<tr>
			<c:url var="editUrl" value="/admin/directory.do">
				<c:param name="action" value="parameterGroupGet"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="directoryId" value="${form.param.directoryId}"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			
			<c:url var="deleteUrl" value="/admin/directory.do">
				<c:param name="action" value="parameterGroupDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>

			<td nowrap="nowrap">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $$.shell.$content(this))"/>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}').done(() => { $$.ajax.load('${form.requestUrl}', $$.shell.$content(this)) })"/>
			</td>

			<td align="right">${item.id}</td>
			<td>${item.title}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="state" value=""/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>