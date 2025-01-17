<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="${form.requestURI}" styleClass="in-mr1">
	<input type="hidden" name="method" value="queueList"/>

	<c:url var="url" value="${form.requestURI}">
		<c:param name="method" value="queueGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>

	<ui:button type="add" onclick="$$.ajax.loadContent('${url}', this)"/>

	<ui:input-text name="filter" value="${form.param.filter}" size="40" placeholder="${l.l('Filter')}" title="${l.l('Фильтр по наименованию, конфигурации')}"
		onSelect="$$.ajax.loadContent(this); return false;"/>

	<ui:page-control/>
</html:form>


<table class="data mt1 hl">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="100%">${l.l('Title')}</td>
		<td>&nbsp;</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<c:url var="editUrl" value="${form.requestURI}">
				<c:param name="method" value="queueGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteUrl" value="${form.requestURI}">
				<c:param name="method" value="queueDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="duplicateUrl" value="${form.requestURI}">
				<c:param name="method" value="queueDuplicate"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>

			<td nowrap="nowrap">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.loadContent('${editUrl}', this)"/>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}', {control: this}).done(
					() => $$.ajax.loadContent('${form.requestUrl}', this)
				)"/>
			</td>
			<td>${item.id}</td>
			<td>${item.title}</td>
			<td>
				<p:check action="ru.bgcrm.struts.action.admin.ProcessAction:queueDuplicate">
					<button type="button" class="btn-grey btn-small icon" title="${l.l('Создать копию')}"
						onclick="
							if (!confirm('${l.l('Создать копию очереди?')}')) return;
							$$.ajax.post('${duplicateUrl}').done(() => $$.ajax.loadContent('${form.requestUrl}', this))">
						<i class="ti-layers"></i>
					</button>
				</p:check>
			</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="${l.l('Очереди процессов')}"/>