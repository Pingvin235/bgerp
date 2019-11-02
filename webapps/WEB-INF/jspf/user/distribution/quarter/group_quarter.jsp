<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="groupQuarters" value="${form.response.data.groupQuarters}"/>
<c:forEach var="quarter" items="${groupQuarters}">
	<option value="${quarter.id}">${quarter.title}</option>
</c:forEach>