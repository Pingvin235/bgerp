<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="new_error_extract.jsp"%>

<c:if test="${not empty error}">
	<div class="error" style="width: 100%;">Ошибка: ${error}</div>
</c:if>