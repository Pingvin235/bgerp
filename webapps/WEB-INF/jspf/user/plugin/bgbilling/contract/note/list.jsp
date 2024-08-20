<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="${form.httpRequestURI}">
	<c:param name="method" value="getMemo"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<ui:button type="add" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())"/>

<table class="data mt1 hl" id="${uiid}">
	<tr>
		<td width="30">&nbsp;</td>
		<td width="100%">Тема</td>
		<td>Дата</td>
		<td>Пользователь</td>
	</tr>
	<c:forEach var="note" items="${frd.list}" varStatus="status">
		<tr>
			<td align="center" nowrap="nowrap">
				<c:url var="editUrl" value="${url}">
					<c:param name="id" value="${note.id}"/>
				</c:url>
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $('#${uiid}').parent())"/>

				<c:url var="deleteUrl" value="${form.httpRequestURI}">
					<c:param name="method" value="deleteMemo"/>
					<c:param name="billingId" value="${form.param.billingId}"/>
					<c:param name="contractId" value="${form.param.contractId}"/>
					<c:param name="id" value="${note.getId()}"/>
				</c:url>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}').done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))"/>
			</td>
			<td>${note.title}</td>
			<td nowrap="nowrap">${tu.format(note.dateTime, 'ymdhms')}</td>
			<td nowrap="nowrap">${note.user}</td>
		</tr>
	</c:forEach>
</table>
