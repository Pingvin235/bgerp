<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty error}">
	<table style="width: 100%;">
		<tr>
			<td class="error">Ошибка: ${error}</td>
		</tr>
	</table>
</c:if>