<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty error}">
	<tr>
		<td class="error">Ошибка: ${error}</td>
	</tr>
</c:if>