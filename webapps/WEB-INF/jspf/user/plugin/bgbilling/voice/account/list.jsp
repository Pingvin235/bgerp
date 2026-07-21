<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="baseUrl" value="${form.requestURI}">
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<c:url var="editUrl" value="${baseUrl}">
	<c:param name="method" value="accountEdit"/>
</c:url>

<c:url var="deleteUrl" value="${baseUrl}">
	<c:param name="method" value="accountDelete"/>
</c:url>

<ui:button type="add" onclick="$$.ajax.load('${editUrl}', this.parentNode)"/>

<table class="data hl mt1">
	<tr>
		<td width="1em">&nbsp;</td>
		<td>ID</td>
		<td>Устройство</td>
		<td>Тип</td>
		<td>Номер</td>
		<td>Период</td>
		<td>Статус</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td class="nowrap">
				<c:url var="url" value="${editUrl}">
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${url}', this.closest('table').parentNode)"/>

				<c:url var="url" value="${deleteUrl}">
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${url}').done(() => { $$.ajax.load('${form.requestUrl}', this.closest('table').parentNode) })"/>
			</td>
			<td>${item.id}</td>
			<td>${item.deviceTitle}</td>
			<td>${item.typeTitle}</td>
			<td>${item.number}</td>
			<td class="nowrap">${tu.format(item.dateFrom, 'ymd')} - ${tu.format(item.dateTo, 'ymd')}</td>
			<td>${item.status.title}</td>
		</tr>
	</c:forEach>
</table>