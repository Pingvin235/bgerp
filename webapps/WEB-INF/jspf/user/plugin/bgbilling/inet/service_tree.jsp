<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="${form.httpRequestURI}">
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
	<c:param name="returnChildUiid" value="${uiid}"/>
</c:url>

<ui:button type="add" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())"/>

<c:set var="uiidMenu" value="${u:uiid()}"/>
<ui:popup-menu id="${uiidMenu}"/>

<table id="${uiid}" class="data hl mt1">
	<tr>
		<td>Сервис</td>
		<td>Тип</td>
		<td>Период</td>
		<td>Статус</td>
		<td>Состояние</td>
		<td>ID</td>
		<td width="30">&nbsp;</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr data-tt-id="${item.id}" data-tt-parent-id="${item.parentId}">
			<td>${item.title}</td>
			<td>${item.typeTitle}</td>
			<td>${tu.formatPeriod(item.dateFrom, item.dateTo, 'ymd')}</td>
			<td>${item.statusTitle}</td>
			<td>${item.devStateTitle}</td>
			<td>${item.id}</td>

			<td nowrap="nowrap">
				<c:url var="serviceMenuUrl" value="${url}">
					<c:param name="action" value="serviceMenu"/>
					<c:param name="id" value="${item.id}"/>
					<c:param name="deviceId" value="${item.deviceId}"/>
				</c:url>
				<button type="button" class="menu btn-white btn-small icon" title="Управление состоянием" onclick="
					$$.ajax
						.load('${serviceMenuUrl}', document.getElementById('${uiidMenu}'), {control: this})
						.done(() => $$.ui.menuInit($(this), $('#${uiidMenu}'), 'left', true));
				"><i class="ti-more"></i></button>

				<c:url var="editUrl" value="${url}">
					<c:param name="action" value="serviceGet"/>
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $('#${uiid}').parent())"/>

				<c:url var="deleteUrl" value="${form.httpRequestURI}">
					<c:param name="action" value="serviceDelete"/>
					<c:param name="contractId" value="${form.param.contractId}"/>
					<c:param name="billingId" value="${form.param.billingId}"/>
					<c:param name="moduleId" value="${form.param.moduleId}"/>
					<c:param name="id" value="${item.id}"/>
				</c:url>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}', {control: this}).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))"/>
			</td>
		</tr>
	</c:forEach>
</table>

<script>
	$(function() {
		$("#${uiid}").treetable({
			expandable: true
		});
	})
</script>