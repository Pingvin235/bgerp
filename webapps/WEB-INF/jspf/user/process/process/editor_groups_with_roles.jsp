<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Выберите группы решения')}</h1>

<c:set var="process" value="${frd.process}" />
<c:set var="processType" value="${process.type}"/>

<html:form action="${form.requestURI}">
	<html:hidden property="id" />
	<input type="hidden" name="method" value="processGroupsUpdate" />

	<c:forEach var="role" items="${ctxUserGroupRoleList}">
		<c:if test="${process.roleSet.contains(role.id)
					 or processType.properties.getAllowedGroups().roleIds.contains(role.id)
					 or empty processType.properties.getAllowedGroups()}">
			<h2>${role.title}</h2>
			<ui:select-mult hiddenName="groupRole"
				list="${ctxUserCache.getUserGroupRoleFullTitledList(role.id)}" values="${process.groups.groupRoleIds}"
				availableIdSet="${processType.properties.getAllowedGroups(role.id).groupRoleIds}"
				style="width: 100%;"/>
		</c:if>
	</c:forEach>

	<%@ include file="editor_grex_save_cancel.jsp"%>
</html:form>