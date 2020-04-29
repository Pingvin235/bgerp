<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="user" value="${form.response.data.user}"/>
<c:if test="${not empty user}">
	<shell:title text="${form.response.data.user.title}"/>

	<h2>${l.l('Параметры')}</h2>

	<c:url var="url" value="/user/parameter.do">
		<c:param name="action" value="parameterList" />
		<c:param name="objectType" value="user"/>
		<c:param name="id" value="${user.id}"/>
		<c:forEach var="pid" items="${config.showParamIds}">
			<c:param name="paramId" value="${pid}"/>
		</c:forEach>
		<c:param name="globalReadOnly" value="1"/>
		<c:param name="logDisable" value="1" />
		<c:param name="showId" value="0" />
	</c:url>
	<c:import url="${url}"/>
</c:if>
