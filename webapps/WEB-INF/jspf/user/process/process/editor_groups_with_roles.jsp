<%@page import="ru.bgcrm.model.process.ProcessType"%>
<%@page import="ru.bgcrm.model.process.TypeProperties"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.HashMap"%>
<%@page import="ru.bgcrm.model.user.Group"%>
<%@page import="ru.bgcrm.cache.UserCache"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="ru.bgcrm.model.process.ProcessGroup"%>
<%@page import="java.util.Set"%>
<%@page import="ru.bgcrm.model.IdTitle"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Выберите группы решения')}</h1>

<c:set var="process" value="${form.response.data.process}" />
<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>
<c:set var="processGroups" value="${process.processGroups}"/>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/process" styleId="${uiid}">
	<html:hidden property="id" />
	<input type="hidden" name="action" value="processGroupsUpdate" />

	<c:forEach var="role" items="${ctxUserGroupRoleList}">
		<c:if test="${process.roleSet.contains(role.id)
					 or processType.properties.allowedRoleSet.contains(role.id)
					 or empty processType.properties.getAllowedGroupsSet()}">
			<h2>${role.title}</h2>

			<u:sc>
				<c:set var="groups" value="${processGroups}"/>
				<%@ include file="/WEB-INF/jspf/groups_list.jsp"%>

				<%
					TypeProperties props = ((ProcessType)pageContext.getAttribute( "processType" )).getProperties();

					int roleId = ((IdTitle)pageContext.getAttribute( "role" )).getId();
					Set<String> allowedGroups = new HashSet<String>();

					for( Integer groupId : props.getAllowedGroupsSet( role.getId() ) )
					{
						allowedGroups.add( groupId + ":" + role.getId() );
					}
					if( allowedGroups.size() > 0 )
					{
						pageContext.setAttribute( "availableIdSet", allowedGroups );
					}
				%>
				<c:set var="hiddenName" value="groupRole"/>
				<c:set var="style" value="width: 100%;"/>
				<%@ include file="/WEB-INF/jspf/select_mult.jsp"%>
			</u:sc>
		</c:if>
	</c:forEach>

	<%@ include file="editor_grex_save_cancel.jsp"%>
</html:form>