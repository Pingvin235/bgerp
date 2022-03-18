<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/process" styleClass="in-mr1">
	<input type="hidden" name="action" value="statusList"/>
	<input type="hidden" name="id" value="-1"/>
	<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>

	<c:url var="url" value="/admin/process.do">
		<c:param name="action" value="statusGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>

	<ui:button type="add" onclick="$$.ajax.load('${url}', $$.shell.$content(this))"/>

	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td>${l.l('Позиция')}</td>
		<td width="100%">${l.l('Наименование')}</td>
		<td>&nbsp;</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="/admin/process.do">
				<c:param name="action" value="statusGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteUrl" value="/admin/process.do">
				<c:param name="action" value="statusDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>

			<td nowrap="nowrap">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $$.shell.$content(this))"/>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}').done(() => { $$.ajax.load('${form.requestUrl}', $$.shell.$content(this)) })"/>
			</td>

			<td>${item.id}</td>
			<td>${item.pos}</td>
			<td>${item.title}</td>

			<c:set var="showId" value="${u:uiid()}" />
			<c:set var="statusId" value="${item.id}" />

			<td align="center">
				<c:url var="showUrl" value="/admin/process.do">
					<c:param name="action" value="statusUseProcess" />
					<c:param name="statusId" value="${statusId}" />
				</c:url>

				<button type="button" class="btn-grey btn-small icon" title="${l.l('Где используется')}"
					onclick="$(this).hide(); $$.ajax.load('${showUrl}', $('#${showId}'));">
					<i class="ti-search"></i>
				</button>

				<div id="${showId}">
				</div>
			</td>
		</tr>
	</c:forEach>
</table>

<shell:title ltext="Статусы процессов"/>
<shell:state help="kernel/process/index.html#status"/>