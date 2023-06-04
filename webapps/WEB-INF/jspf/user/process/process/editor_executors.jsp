<%@page import="org.bgerp.app.l10n.Localizer"%>
<%@page import="org.bgerp.app.l10n.Localization"%>
<%@page import="ru.bgcrm.struts.form.DynActionForm"%>
<%@page import="ru.bgcrm.model.user.User"%>
<%@page import="ru.bgcrm.model.process.Process"%>
<%@page import="ru.bgcrm.model.process.ProcessExecutor"%>
<%@page import="ru.bgcrm.model.process.ProcessGroup"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.List"%>
<%@page import="ru.bgcrm.cache.UserCache"%>
<%@page import="org.bgerp.model.base.IdTitle"%>
<%@page import="ru.bgcrm.model.IdStringTitle"%>
<%@page import="java.util.ArrayList"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Выберите исполнителей')}</h1>

<c:set var="process" value="${form.response.data.process}"/>
<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>

<c:set var="mainUiid" value="${u:uiid()}"/>

<html:form action="/user/process" styleId="${mainUiid}">
	<html:hidden property="id"/>
	<input type="hidden" name="action" value="processExecutorsUpdate"/>

	<c:set var="uiid" value="${u:uiid()}"/>
	<c:set var="perm" value="${ctxUserCache.getPerm(form.user.id, 'ru.bgcrm.struts.action.ProcessAction:processExecutorsUpdate' )}"/>
	<c:set var="allowedGroups"  value="${u.toIntegerSet(perm['allowOnlyGroups'])}"/>

	<c:forEach var="role" items="${ctxUserGroupRoleList}">
		<c:forEach var="group" items="${ctxUserGroupFullTitledList}">
			<c:remove var="groupIs"/>
			<c:forEach  var="processGroup" items="${process.groups}">
				<c:if test="${processGroup.groupId eq group.id and processGroup.roleId eq role.id}">
					<c:set var="groupIs" value="true"/>
				</c:if>
			</c:forEach>

			<c:if test="${groupIs and group.allowExecutorsSet and
						(empty allowedGroups or allowedGroups.contains(group.id))}">

				<h2>${group.title}
					<c:if test="${role.id ne 0}">
						(${role.title})
					</c:if>
				</h2>

				<input type="hidden" name="group" value="${group.id}:${role.id}"/>

				<u:sc>
					<%
						DynActionForm form = (DynActionForm)request.getAttribute( "form" );

						IdTitle role = (IdTitle)pageContext.getAttribute( "role" );
						IdTitle group = (IdTitle)pageContext.getAttribute( "group" );

						Set<ProcessExecutor> executors = ((Process)pageContext.getAttribute( "process" )).getExecutors();

						Localizer l = (Localizer) request.getAttribute("l");
						IdStringTitle meItem = null;

						List<IdStringTitle> list = new ArrayList<>(executors.size());
						for( User user : UserCache.getUserList() )
						{
							// TODO: Наверное, лучше сделать фильтр не по текущим группам, а по когда-либо присутствующим.
							if (!user.getGroupIds().contains(group.getId()) ||
								user.getStatus() != User.STATUS_ACTIVE)
								continue;

							String userGroupAndRole = user.getId() + ":" + group.getId() + ":" + role.getId();
							if (user.getId() == form.getUserId())
								meItem = new IdStringTitle(userGroupAndRole, user.getTitle() + " " + l.l("[you]"));
							else
								list.add(new IdStringTitle(userGroupAndRole, user.getTitle()));
						}

						if (meItem != null)
							list.add(0, meItem);

						Set<String> values = new HashSet<>(executors.size());
						for (ProcessExecutor ex : executors)
							if (UserCache.getUser(ex.getUserId()).getStatus() == 0)
								values.add(ex.getUserId() + ":" + ex.getGroupId() + ":" + ex.getRoleId());

						pageContext.setAttribute("list", list);
						pageContext.setAttribute("values", values);
					%>

					<ui:select-mult hiddenName="executor" list="${list}" values="${values}" style="width: 100%;"/>
				</u:sc>
			</c:if>

		</c:forEach>
	</c:forEach>

	<%@ include file="editor_grex_save_cancel.jsp"%>
</html:form>