<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/user">
	<input type="hidden" name="method" value="permsetList"/>
	<input type="hidden" name="pageableId" value="permsetList"/>

	<c:url var="url" value="/admin/user.do">
		<c:param name="method" value="permsetGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<ui:button type="add" onclick="$$.ajax.loadContent('${url}', this)"/>

	<ui:input-text name="filter" styleClass="ml1" value="${form.param.filter}" placeholder="${l.l('Filter')}" size="40"
		onSelect="$$.ajax.loadContent(this)"
		title="${l.l('Фильтр по наименованию, комментарию, конфигурации, параметрам действий')}"/>

	<ui:page-control/>
</html:form>

<table class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="30%">${l.l('Title')}</td>
		<td width="70%">${l.l('Comment')}</td>
	</tr>
	<c:forEach var="permset" items="${frd.list}">
		<tr>
			<c:url var="editUrl" value="/admin/user.do">
				<c:param name="method" value="permsetGet"/>
				<c:param name="id" value="${permset.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteUrl" value="/admin/user.do">
				<c:param name="method" value="permsetDelete"/>
				<c:param name="id" value="${permset.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="$$.ajax.loadContent('${form.requestUrl}')"/>

			<c:set var="uiid" value="${u:uiid()}"/>

			<td nowrap="nowrap" id="${uiid}">
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.loadContent('${editUrl}', this)"/>
				<ui:button type="del" styleClass="btn-small" onclick="
					$$.ajax
						.post('${deleteUrl}', {control: this})
						.done(() => $$.ajax.loadContent('${form.requestUrl}', this))"/>

				<button type="button" class="btn-white btn-small icon"
					title="${l.l('Заменить права набора на права из другого набора')}"
					onclick="$('#${uiid} > input').hide(); $('#${uiid} > form').css('display', 'inline');"><i class="ti-import"></i></button>

				<html:form style="display: none;" action="/admin/user" onsubmit="return false;" styleClass="ml1">
					<input type="hidden" name="method" value="permsetReplacePermissions"/>
					<input type="hidden" name="id" value="${permset.id}"/>

					<ui:select-single list="${ctxUserPermsetList}" hiddenName="fromId" style="width: 200px;" placeholder="Выберите набор"/>

					<button
						type="button" class="btn-grey ml1"
						onclick="if (confirm('Вы уверены, что хотите заменить права\nна права из выбранного набора?')) $$.ajax.post(this.form).done(() => { $('#${uiid} > form').hide(); $('#${uiid} > input').show(); })">OK</button>
					<button
						type="button" class="btn-grey"
						onclick="$('#${uiid} > form').hide(); $('#${uiid} > input').show();">${l.l('Cancel')}</button>
				</html:form>
			</td>

			<td>${permset.id}</td>
			<td>${permset.title}</td>
			<td>${permset.comment}</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="${l.l('Наборы прав')}"/>
<shell:state/>