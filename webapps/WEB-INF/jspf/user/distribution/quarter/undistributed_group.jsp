<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="undistGroups" value="${form.response.data.undistGroups}" />
<c:forEach var="group" items="${undistGroups}">
	<option value="${group.id}">${group.title}</option>
</c:forEach>