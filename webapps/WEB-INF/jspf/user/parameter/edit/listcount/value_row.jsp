<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	frd.listValues - available values, put by 'listValues' both on editor opening and on a row adding
	multiple       - multiple values
Optional for a value:
	itemId         - id
	itemCount      - count
--%>

<tr>
	<td>
		<ui:select-single list="${frd.listValues}" name="itemId" value="${itemId}" styleClass="w100p"/>
	</td>
	<td>
		<ui:input-decimal name="itemCount" value="${itemCount}" digits="2" size="4" title="${l.l('Use dot as a decimal separator')}"/>
	</td>
	<td>
		<button type="button" class="btn-white btn-small icon" onclick="$$.param.listcount.delValue(this, ${multiple})"><i class='ti-trash'></i></button>
	</td>
</tr>