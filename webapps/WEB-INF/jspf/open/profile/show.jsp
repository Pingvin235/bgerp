<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="user" value="${frd.user}"/>
<c:if test="${not empty user}">
	<shell:title text="${user.title}"/>

	<u:sc>
		<c:set var="objectType" value="user"/>
		<c:set var="paramIds" value="${config.showParamIds}"/>
		<%@ include file="/WEB-INF/jspf/open/parameter_list.jsp"%>
	</u:sc>
</c:if>
