<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	value - ParameterPhoneValue
--%>
<u:sc>
	<c:set var="addButtonUiid" value="${u:uiid()}"/>
	<table class="data">
		<tr>
			<td width="50%">${l.l('Number')}</td>
			<td width="50%">${l.l('Comment')}</td>
			<td><ui:button type="add" id="${addButtonUiid}" styleClass="btn-small" onclick="$$.param.phone.addValue(this)"/></td>
		</tr>
		<c:forEach var="item" items="${value.itemList}">
			<%@ include file="value_row.jsp"%>
		</c:forEach>
	</table>
	<c:if test="${empty value.itemList}">
		<script>
			$(function() {
				document.getElementById('${addButtonUiid}').click();
			})
		</script>
	</c:if>
</u:sc>