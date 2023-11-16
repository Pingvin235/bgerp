<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<%@ include file="../create_button.jsp"%>

<table class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="50%">${l.l('Название')}</td>
		<td width="50%">${l.l('Шаблон')}</td>
	</tr>
	<c:forEach var="item" items="${patternList}">
		<tr>
			<c:url var="editUrl" value="/admin/directory.do">
				<c:param name="action" value="patternTitleGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="directoryId" value="${form.param.directoryId}"/>
			</c:url>

			<c:url var="deleteUrl" value="/admin/directory.do">
				<c:param name="action" value="patternTitleDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>

			<td nowrap="nowrap">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.loadContent('${editUrl}', this)"/>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}', {control: this}).done(() => $$.ajax.loadContent('${form.requestUrl}', this))"/>
			</td>

			<td align="right">${item.id}</td>
			<td>${item.title}</td>
			<td>${item.pattern}</td>
		</tr>
	</c:forEach>
</table>

<shell:state/>