<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<td nowrap="nowrap">
	<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.loadContent('${editUrl}', this)"/>
	<ui:button type="del" styleClass="btn-small"
		onclick="$$.ajax.post('${delUrl}').done(() => { $$.ajax.loadContent('${form.requestUrl}', this) })"/>
</td>