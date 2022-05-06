<%@page import="ru.bgerp.l10n.Localizer"%>
<%@page import="ru.bgerp.l10n.Localization"%>
<%@page import="ru.bgcrm.event.user.UserListEvent"%>
<%@page import="ru.bgcrm.event.EventProcessor"%>
<%@page import="ru.bgcrm.struts.form.DynActionForm"%>
<%@page import="ru.bgcrm.model.user.User"%>
<%@page import="ru.bgcrm.model.process.Process"%>
<%@page import="ru.bgcrm.model.process.ProcessExecutor"%>
<%@page import="ru.bgcrm.model.process.ProcessGroup"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="ru.bgcrm.model.user.Group"%>
<%@page import="java.util.List"%>
<%@page import="ru.bgcrm.cache.UserCache"%>
<%@page import="ru.bgcrm.model.IdTitle"%>
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
	<c:set var="perm" value="${p:get( form.user.id, 'ru.bgcrm.struts.action.ProcessAction:processExecutorsUpdate' )}"/>
	<c:set var="allowedGroups"  value="${u.toIntegerSet(perm['allowOnlyGroups'])}"/>

	<c:forEach var="role" items="${ctxUserGroupRoleList}">
		<c:forEach var="group" items="${ctxUserGroupFullTitledList}">
			<c:remove var="groupIs"/>
			<c:forEach  var="processGroup" items="${process.processGroups}">
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

						Set<ProcessExecutor> executors = ((Process)pageContext.getAttribute( "process" )).getProcessExecutors();

						String meGroupAndRole = null;

						List<Map<String, String>> list = new ArrayList<Map<String, String>>( executors.size() );
						for( User user : UserCache.getUserList() )
						{
							//TODO: Наверное, лучше сделать фильтр не по текущим группам, а по когда-либо присутствующим.
							if (!user.getGroupIds().contains(group.getId()) ||
								user.getStatus() != User.STATUS_ACTIVE)
							{
								continue;
							}

							String userGroupAndRole = user.getId() + ":" + group.getId() + ":" + role.getId();

							Map<String, String> idTitle = new HashMap<String, String>();
							list.add( idTitle );

							idTitle.put( "id", userGroupAndRole );
							idTitle.put( "title", user.getTitle() );

							if( user.getId() == form.getUserId() )
							{
								meGroupAndRole = userGroupAndRole;
							}
						}

						Localizer l = (Localizer) request.getAttribute("l");

						if( meGroupAndRole != null )
						{
							Map<String, String> idTitle = new HashMap<String, String>();
							list.add( 0, idTitle );

							idTitle.put( "id", meGroupAndRole );
							idTitle.put( "title", l.l("** Я **") );
							idTitle.put( "fake", "1" );
						}

						Set<String> values = new HashSet<String>( executors.size() );
						for( ProcessExecutor ex : executors )
						{
							if( UserCache.getUser( ex.getUserId() ).getStatus() == 0 )
							{
								values.add( ex.getUserId() + ":" + ex.getGroupId() + ":" + ex.getRoleId() );
							}
						}

						EventProcessor.processEvent(new UserListEvent(form, list), null);

						pageContext.setAttribute( "list", list );
						pageContext.setAttribute( "values", values );
					%>

					<ui:select-mult
						hiddenName="executor" list="${list}" values="${values}"
						style="width: 100%;" fakeHide="true"
					/>
				</u:sc>
			</c:if>

		</c:forEach>
	</c:forEach>

	<%@ include file="editor_grex_save_cancel.jsp"%>
</html:form>