<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="showCode" value="$$.ajax.loadContent(this);"/>

<html:form action="/admin/user" styleClass="in-mr1 in-mb1" styleId="${uiid}" style="vertical-align: middle;">
	<c:url var="url" value="/admin/user.do">
		<c:param name="method" value="userGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<ui:button type="add" onclick="$$.ajax.loadContent('${url}', this)"/>

	<input type="hidden" name="method" value="userList"/>
	<input type="hidden" name="pageableId" value="userList"/>

	<ui:input-text name="filter" showOutButton="false" value="${form.param.filter}" size="20" placeholder="${l.l('Filter')}" title="${l.l('Filter by ID, title, login, comment')}"
		onSelect="$$.ajax.loadContent(this);"/>

	<%@ include file="user_status_const.jsp"%>

	<ui:combo-single hiddenName="status" value="${form.param.status}" onSelect="${showCode}"
		prefixText="${l.l('Status')}:" widthTextValue="70px">
		<jsp:attribute name="valuesHtml">
			<li value="${STATUS_ACTIVE}">${l.l('Активные')}</li>
			<li value="${STATUS_DISABLED}">${l.l('Заблокированные')}</li>
			<li value="${STATUS_EXTERNAL}">${l.l('Внешние')}</li>
			<li value="-1">${l.l('All')}</li>
		</jsp:attribute>
	</ui:combo-single>

	<c:set var="perm" value="${ctxUser.getPerm('ru.bgcrm.struts.action.admin.UserAction:userList')}"/>
	<c:if test="${empty perm['allowOnlyGroups'] or not empty perm['allowFilterGroups']}">
		<ui:select-single hiddenName="group"
			list="${ctxUserGroupFullTitledList}" map="${ctxUserGroupFullTitledMap}"
			availableIdSet="${u.toIntegerSet(perm['allowFilterGroups'])}"
			value="${form.param.group}"
			onSelect="${showCode}" placeholder="${l.l('Group')}" style="width: 200px;"/>

		<ui:date-time paramName="date" placeholder="${l.l('Гр. на дату')}" value="${form.param.date}"/>
	</c:if>

	<ui:select-single list="${ctxUserPermsetList}" hiddenName="permset" value="${form.param.permset}"
		onSelect="${showCode}" placeholder="${l.l('Набор прав')}" style="width: 200px;"/>

	<ui:button type="out" onclick="$$.ajax.loadContent(this);"/>

	<ui:page-control/>
</html:form>

<table class="data hl">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td>${l.l('Status')}</td>
		<td>${l.l('Title')}</td>
		<td>${l.l('Логин')}</td>
		<td>${l.l('Наборы прав')}</td>
		<td>${l.l('Groups')}</td>
		<td>${l.l('Комментарий')}</td>
	</tr>
	<c:forEach var="item" items="${frd.list}">
		<tr>
			<c:url var="editUrl" value="/admin/user.do">
				<c:param name="method" value="userGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteUrl" value="/admin/user.do">
				<c:param name="method" value="userDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>

			<td nowrap="nowrap">
				<c:if test="${item.status ne STATUS_EXTERNAL}">
					<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.loadContent('${editUrl}', this)"/>
				</c:if>
				<ui:button type="del" styleClass="btn-small" onclick="
					$$.ajax
						.post('${deleteUrl}', {control: this})
						.done(() => $$.ajax.loadContent('${form.requestUrl}', this))"/>

				<c:set var="user" scope="request" value="${item}"/>
				<plugin:include endpoint="admin.user.action.jsp"/>
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
			<td>${u.getObjectTitles(ctxUserPermsetMap, item.permsetIds)}</td>
			<td>${u.getObjectTitles(ctxUserGroupFullTitledList, item.groupIds)}</td>
			<td>${item.description}</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="${l.l('Пользователи')}"/>
<shell:state/>
