<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	itemId or form.param.itemId
	itemTitle or form.param.itemTitle
	itemCount or form.param.itemCount
--%>

<tr>
	<td>
		<input type="hidden" name="itemId" value="${not empty itemId ? itemId : form.param.itemId}"/>
		${not empty itemTitle ? itemTitle : form.param.itemTitle}
	</td>
	<td>
		<input type="text" name="itemCount" value="${not empty itemCount ? itemCount : form.param.itemCount}" size="4"
			onkeydown="return isNumberKey(event)" title="${l.l('Use dot as a decimal separator')}"/>
	</td>
	<td>
		<button class="btn-white icon" onclick="$(this).closest('tr').remove();"><i class='ti-trash'></i></button>
	</td>
</tr>