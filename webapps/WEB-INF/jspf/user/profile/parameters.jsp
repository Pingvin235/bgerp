<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="requestUserId" value="${form.param.requestUserId}" />

<div>
<c:if test="${requestUserId > 0}">
	<h2>${l.l('Группы')}</h2>

	<c:set var="readOnly" value="true" />
	<c:set var="user" value="${frd.user}" />
	<c:set var="userGroupList" value="${frd.userGroupList}" />
	<%@ include file="/WEB-INF/jspf/admin/user/user/update_usergroup.jsp"%>

	<h2>${l.l('Параметры')}</h2>
</c:if>
<c:if test="${form.userId != '-1' }">
		<div id="userParameters">
			<c:url var="url" value="/user/parameter.do">
				<c:param name="action" value="parameterList" />

				<c:if test="${not empty requestUserId && requestUserId != form.userId}">
					<c:param name="readOnly" value="1"/>
					<c:param name="logDisable" value="1"/>
				</c:if>

				<c:param name="id" value="${requestUserId > 0 ? requestUserId : form.userId}" />
				<c:param name="objectType" value="user" />
			</c:url>
			<c:import url="${url}" />
		</div>
	</c:if>
</div>