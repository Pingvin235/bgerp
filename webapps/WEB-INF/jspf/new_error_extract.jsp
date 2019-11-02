<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${empty error and form.response.status eq 'error'}">
	<c:set var="error" scope="page" value="${form.response.message}"/>
</c:if>
