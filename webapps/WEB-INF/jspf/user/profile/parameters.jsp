<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="requestUserId" value="${form.param.requestUserId}"/>
<c:if test="${requestUserId gt 0}">
	<h1>${l.l('Groups')}</h1>

	<c:set var="readOnly" value="${true}"/>
	<c:set var="user" value="${frd.user}"/>
	<c:set var="userGroupList" value="${frd.userGroupList}"/>
	<%@ include file="/WEB-INF/jspf/admin/user/user/update_usergroup.jsp"%>

	<h1>${l.l('Parameters')}</h1>
	<div>
		<c:url var="url" value="/user/parameter.do">
			<c:param name="method" value="parameterList"/>
			<c:param name="objectType" value="user"/>
			<c:param name="id" value="${requestUserId}"/>

			<c:set var="method" value="${requestUserId eq form.userId ? 'updateOwnParameter' : 'updateOthersParameter'}"/>

			<c:if test="${not ctxUser.checkPerm('/user/profile:'.concat(method))}">
				<c:param name="readOnly" value="1"/>
			</c:if>
		</c:url>
		<c:import url="${url}" />
	</div>
</c:if>