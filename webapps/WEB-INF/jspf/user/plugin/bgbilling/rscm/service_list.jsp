<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="${form.httpRequestURI}" styleId="${uiid}">
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="contractId"/>
	<input type="hidden" name="action" value="serviceList"/>

	<c:url var="url" value="${form.httpRequestURI}">
		<c:param name="action" value="serviceGet"/>
		<c:param name="contractId" value="${form.param.contractId}"/>
		<c:param name="billingId" value="${form.param.billingId}"/>
		<c:param name="moduleId" value="${form.param.moduleId}"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>

	<ui:button type="add" title="Добавить услугу" onclick="$$.ajax.load('${url}', $(this.form).parent())" styleClass="mr1"/>

	<ui:date-month-days/>

	<ui:button type="out" styleClass="ml1" onclick="$$.ajax.load(this, $(this.form).parent())" title="Вывести"/>

	<ui:page-control/>
</html:form>

<c:set var="uiid" value="${u:uiid()}"/>

<table class="data hl mt1" id="${uiid}">
	<tr>
		<td width="30"></td>
		<td>Дата</td>
		<td width="50%">Услуга</td>
		<td>Кол-во</td>
		<td>Ед. имерения</td>
		<td width="50%">Комментарий</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<td nowrap="nowrap">
				<c:url var="editUrl" value="${url}">
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $(this.closest('table')).parent())"/>
			</td>
			<td nowrap="nowrap">${tu.format(item.date, 'ymd')}</td>
			<td>${item.serviceTitle}</td>
			<td>${item.amount}</td>
			<td>${item.unit}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>