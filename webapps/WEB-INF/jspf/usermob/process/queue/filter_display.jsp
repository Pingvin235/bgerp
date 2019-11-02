<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="displayParams" value="${divParams}"/> 
<c:if test="${not filter.show}">
	<c:set var="displayParams" value="display:none"/>				
</c:if>