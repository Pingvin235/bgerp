<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- id="forParamsReload" --%>
<div>
	<c:url var="url" value="/user/parameter.do">
		<c:param name="action" value="parameterList"/>
		<c:param name="id" value="${process.id}"/>
		<c:param name="objectType" value="process"/>
		<c:param name="header" value="${l.l('Parameters')}"/>
		<c:param name="processTypeId" value="${process.typeId}"/>
		<c:forEach var="paramId" items="${processType.properties.parameterIds}">
			<c:param name="paramId" value="${paramId}"/>
		</c:forEach>
	</c:url>

	<c:remove var="form"/>
	<c:import url="${url}"/>
</div>