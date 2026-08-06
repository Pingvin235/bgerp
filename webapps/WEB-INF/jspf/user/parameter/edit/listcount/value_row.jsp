<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	frd.listValues - available values, put by 'listValues' both on editor opening and on a row adding
	paramId        - the parameter ID, a request parameter of both editor opening and row adding
Optional for a value:
	itemId         - id
	itemCount      - count
--%>

<c:set var="parameter" value="${ctxParameterCache.getParameter(form.param.paramId)}"/>

<tr>
	<td>
		<ui:select-single list="${frd.listValues}" name="itemId" value="${itemId}" styleClass="w100p"/>
	</td>
	<td>
		<ui:input-decimal name="itemCount" value="${itemCount}" digits="2" size="4" title="${l.l('Use dot as a decimal separator')}"/>
	</td>
	<td>
		<button type="button" class="btn-white btn-small icon" onclick="$$.param.listcount.delValue(this, ${parameter.multiple})"><i class='ti-trash'></i></button>
	</td>
</tr>