<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/user">
	<input type="hidden" name="action" value="permsetList"/>
	<input type="hidden" name="pageableId" value="permsetList"/>

	<c:url var="url" value="/admin/user.do">
		<c:param name="action" value="permsetGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<ui:button type="add" onclick="$$.ajax.loadContent('${url}', this)"/>

	<ui:input-text name="filter" styleClass="ml1" value="${form.param.filter}" placeholder="${l.l('Filter')}" size="40"
		onSelect="$$.ajax.loadContent(this)"
		title="${l.l('Фильтр по наименованию, комментарию, конфигурации, параметрам действий')}"/>

	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="30%">${l.l('Title')}</td>
		<td width="70%">${l.l('Комментарий')}</td>
	</tr>
	<c:forEach var="permset" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="/admin/user.do">
				<c:param name="action" value="permsetGet"/>
				<c:param name="id" value="${permset.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteUrl" value="/admin/user.do">
				<c:param name="action" value="permsetDelete"/>
				<c:param name="id" value="${permset.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>

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
					<input type="hidden" name="action" value="permsetReplacePermissions"/>
					<input type="hidden" name="id" value="${permset.id}"/>

					<u:sc>
						<c:set var="list" value="${ctxUserPermsetList}"/>
						<c:set var="hiddenName" value="fromId"/>
						<c:set var="placeholder" value="Выберите набор"/>
						<c:set var="style" value="width: 200px;"/>
						<%@ include file="/WEB-INF/jspf/select_single.jsp"%>
					</u:sc>

					<button
						type="button" class="btn-grey ml1"
						onclick="if( confirm( 'Вы уверены, что хотите заменить права\nна права из выбранного набора?' ) && sendAJAXCommand( formUrl( this.form ) ) ){ $('#${uiid} > form').hide(); $('#${uiid} > input').show(); }">OK</button>
					<button
						type="button" class="btn-grey"
						onclick="$('#${uiid} > form').hide(); $('#${uiid} > input').show();">${l.l('Отмена')}</button>
				</html:form>
			</td>

			<td>${permset.id}</td>
			<td>${permset.title}</td>
			<td>${permset.comment}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="title" value="${l.l('Наборы прав')}"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>