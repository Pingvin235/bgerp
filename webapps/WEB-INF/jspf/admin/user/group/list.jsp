<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="urlList" value="/admin/user.do">
	<c:param name="method" value="groupList"/>
	<c:param name="parentGroupId" value="${form.param.parentGroupId}"/>
</c:url>

<html:form action="/admin/user" styleId="${uiid}" styleClass="in-mr1">
	<input type="hidden" name="method" value="groupList"/>
	<input type="hidden" name="parentGroupId" value="${form.param.parentGroupId}"/>
	<input type="hidden" name="markGroup" value="${form.param.markGroup}"/>

	<c:url var="url" value="/admin/user.do">
		<c:param name="method" value="groupGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
		<c:param name="parentGroupId" value="${form.param.parentGroupId}"/>
	</c:url>
	<ui:button type="add" onclick="$$.ajax.loadContent('${url}', this)"/>

	<c:url value="/admin/user.do" var="url">
		<c:param name="method" value="groupInsertMark"/>
		<c:param name="parentGroupId" value="${form.param.parentGroupId}"/>
		<c:param name="markGroup" value="${form.param.markGroup}"/>
	</c:url>
	<button type="button" class="btn-grey"
		onclick="$$.ajax.post('${url}').done(() => $$.ajax.loadContent('${urlList}', this))">${l.l('Вставить')} [${markGroupString}]</button>

	<ui:input-text name="filter" onSelect="$$.ajax.loadContent(this)" placeholder="${l.l('Filter')}" size="40" value="${form.param['filter']}" title="${l.l('Filter by ID, title, configuration')}"/>

	<ui:page-control/>
</html:form>

<div class="mt1">
	<c:url var="url" value="/admin/user.do">
		<c:param name="method" value="groupList"/>
		<c:param name="parentGroupId" value="0"/>
		<c:param name="markGroup" value="${form.param.markGroup}"/>
	</c:url>

	&nbsp;
	<a href="#" onClick="$$.ajax.loadContent('${url}', this); return false;">${l.l('Groups')}</a>

	<c:forEach var="item" items="${groupPath}" varStatus="status">
		<c:url var="url" value="/admin/user.do">
			<c:param name="method" value="groupList"/>
			<c:param name="parentGroupId" value="${item.id}"/>
			<c:param name="markGroup" value="${form.param.markGroup}"/>
		</c:url>
		/ <a href="#" onClick="$$.ajax.loadContent('${url}', this); return false;">${item.title}</a>
	</c:forEach>
</div>

<table class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="30%">${l.l('Title')}</td>
		<td width="50">${l.l('Подгрупп')}</td>
		<td width="40%">${l.l('Permission Sets')}</td>
		<td width="30%">${l.l('Comment')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<c:url var="editUrl" value="/admin/user.do">
				<c:param name="method" value="groupGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="parentGroupId" value="${item.parentId}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteUrl" value="/admin/user.do">
				<c:param name="method" value="groupDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<td nowrap="nowrap">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.loadContent('${editUrl}', this)"/>
				<ui:button type="del" styleClass="btn-small" onclick="
					$$.ajax
						.post('${deleteUrl}', {control: this})
						.done(() => $$.ajax.loadContent('${form.requestUrl}', this))"/>

				<ui:button type="cut" styleClass="btn-small"
					onclick="$('#${uiid}')[0].markGroup.value=${item.id};
							toPage($('#${uiid}')[0], ${form.page.pageIndex}, ${form.page.pageSize}, '');
							$$.ajax.loadContent($('#${uiid}'), this)"/>
			</td>

			<td>${item.id}</td>

			<td>
				<c:forEach var="items" items="${item.path}" varStatus="status">
					<c:url var="url" value="/admin/user.do">
						<c:param name="method" value="groupList"/>
						<c:param name="parentGroupId" value="${item.id}"/>
						<c:param name="markGroup" value="${form.param.markGroup}"/>
					</c:url>
					<c:if test="${status.last}">
						<a href="#" onclick="$$.ajax.loadContent('${url}', this); return false;">${items.title}</a>
					</c:if>
					<c:if test="${not empty form.param.filter && not status.last}">
						<a href="#" onclick="$$.ajax.loadContent('${url}', this); return false;">${items.title}</a> /
					</c:if>
				</c:forEach>
			</td>

			<td>${item.childCount}</td>
			<td>${u.getObjectTitles(ctxUserPermsetMap, item.permsetIds)}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="${l.l('Группы пользователей')}"/>
<shell:state/>