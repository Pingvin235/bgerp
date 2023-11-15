<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<td colspan="2">
		<ui:form-ok-cancel loadReturn="$$.ajax.loadContent('${form.returnUrl}', this)"/>
	</td>
</tr>