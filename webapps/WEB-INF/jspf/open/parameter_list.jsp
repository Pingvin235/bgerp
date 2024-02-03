<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>${l.l('Parameters')}</h2>

<c:url var="url" value="/open/parameter.do">
	<c:param name="action" value="parameterList" />
	<c:param name="objectType" value="${objectType}"/>
	<c:param name="id" value="${form.id}"/>
	<c:forEach var="pid" items="${paramIds}">
		<c:param name="paramId" value="${pid}"/>
	</c:forEach>
	<c:param name="readOnly" value="1"/>
	<c:param name="logDisable" value="1"/>
	<c:param name="showId" value="0"/>
</c:url>
<c:import url="${url}"/>