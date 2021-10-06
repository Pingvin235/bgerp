<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="showCode" value="$$.ajax.load($('#${uiid}'), $$.shell.$content());"/>

<html:form action="admin/user" styleClass="in-mr1 in-mb1" styleId="${uiid}" style="vertical-align: middle;">
	<c:url var="url" value="/admin/user.do">
		<c:param name="action" value="userGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<ui:button type="add" onclick="$$.ajax.load('${url}', $$.shell.$content(this))"/>

	<input type="hidden" name="action" value="userList"/>
	<input type="hidden" name="pageableId" value="userList"/>

	<ui:input-text name="title" showOutButton="false" value="${form.param['title']}" size="20" placeholder="${l.l('Фильтр')}" title="${l.l('Фильтр по наименованию')}"
		onSelect="$$.ajax.load(this.form, $$.shell.$content(this));"/>

	<%@ include file="user_status_const.jsp"%>

	<ui:combo-single hiddenName="status" value="${form.param.status}" onSelect="${showCode}"
		prefixText="${l.l('Статус')}:" widthTextValue="70px">
		<jsp:attribute name="valuesHtml">
			<li value="${STATUS_ACTIVE}">${l.l('Активные')}</li>
			<li value="${STATUS_DISABLED}">${l.l('Заблокированные')}</li>
			<li value="${STATUS_EXTERNAL}">${l.l('Внешние')}</li>
			<li value="-1">${l.l('Все')}</li>
		</jsp:attribute>
	</ui:combo-single>

	<c:set var="perm" value="${p:get(form.user.id, 'ru.bgcrm.struts.action.admin.UserAction:userList')}"/>
	<c:if test="${empty perm['allowOnlyGroups'] or not empty perm['allowFilterGroups']}">
		<ui:select-single hiddenName="group"
			list="${ctxUserGroupFullTitledList}" map="${ctxUserGroupFullTitledMap}"
			availableIdSet="${u.toIntegerSet(perm['allowFilterGroups'])}"
			value="${form.param.group}"
			onSelect="${showCode}" placeholder="${l.l('Группа')}" style="width: 200px;"/>

		<ui:date-time paramName="date" placeholder="${l.l('Гр. на дату')}" value="${form.param.date}"/>
	</c:if>

	<ui:select-single list="${ctxUserPermsetList}" hiddenName="permset" value="${form.param.permset}"
		onSelect="${showCode}" placeholder="${l.l('Набор прав')}" style="width: 200px;"/>

	<ui:button type="out" onclick="$$.ajax.load(this.form, $$.shell.$content(this));"/>

	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<table style="width: 100%;" class="data">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td>${l.l('Статус')}</td>
		<td>${l.l('Наименование')}</td>
		<td>${l.l('Логин')}</td>
		<td>${l.l('Наборы прав')}</td>
		<td>${l.l('Группы')}</td>
		<td>${l.l('Комментарий')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
		 	<c:url var="editUrl" value="/admin/user.do">
				<c:param name="action" value="userGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteUrl" value="/admin/user.do">
				<c:param name="action" value="userDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>

			<td nowrap="nowrap">
				<c:if test="${item.status ne STATUS_EXTERNAL}">
					<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $$.shell.$content(this))"/>
				</c:if>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}').done(() => { $$.ajax.load('${form.requestUrl}', $$.shell.$content(this)) })"/>
			</td>

			<td>${item.id}</td>
			<td>
				<c:choose>
					<c:when test="${item.status eq STATUS_ACTIVE}">${l.l('Активен')}</c:when>
					<c:when test="${item.status eq STATUS_DISABLED}">${l.l('Заблокирован')}</c:when>
					<c:when test="${item.status eq STATUS_EXTERNAL}">${l.l('Внешний')}</c:when>
					<c:otherwise>${l.l('Неизвестный статус')} (${item.status})</c:otherwise>
				</c:choose>
			</td>
			<td><ui:user-link id="${item.id}"/></td>
			<td>${item.login}</td>
			<td>${u:orderedObjectTitleList( ctxUserPermsetMap, item.permsetIds )}</td>
			<td>${u:objectTitleList( ctxUserGroupFullTitledList, item.groupIds )}</td>
			<td>${item.description}</td>
		</tr>
	</c:forEach>
</table>

<shell:title ltext="Пользователи"/>
<shell:state text=""/>
