<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Выберите группы решения')}</h1>

<c:set var="process" value="${form.response.data.process}" />
<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/process" styleId="${uiid}">
	<html:hidden property="id" />
	<input type="hidden" name="action" value="processGroupsUpdate" />

	<c:forEach var="role" items="${ctxUserGroupRoleList}">
		<c:if test="${process.roleSet.contains(role.id)
					 or processType.properties.allowedRoleSet.contains(role.id)
					 or empty processType.properties.getAllowedGroupsSet()}">
			<h2>${role.title}</h2>
			<ui:select-mult hiddenName="groupRole"
				list="${ctxUserCache.getUserGroupRoleFullTitledList(role.id)}" values="${process.groups.groupRoleIds}"
				availableIdSet="${processType.properties.getAllowedGroupsSet(role.id)}"
				style="width: 100%;"/>
		</c:if>
	</c:forEach>

	<%@ include file="editor_grex_save_cancel.jsp"%>
</html:form>