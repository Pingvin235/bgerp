<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	frd.values - list of ParameterEmailValue
--%>
<u:sc>
	<c:set var="addButtonUiid" value="${u:uiid()}"/>
	<table class="data">
		<tr>
			<td width="50%">Email or ${l.l('Domain')}</td>
			<td width="50%">${l.l('Name')}</td>
			<td><ui:button type="add" id="${addButtonUiid}" styleClass="btn-small" onclick="$$.param.email.addValue(this)"/></td>
		</tr>
		<c:forEach var="item" items="${frd.values}">
			<tr>
				<td><input type="text" name="address" value="${item.value}" class="w100p"/></td>
				<td><input type="text" name="name" value="${item.comment}" class="w100p"/></td>
				<td><button type="button" class="btn-white btn-small icon" onclick="$$.param.email.delValue(this)"><i class="ti-trash"></i></button></td>
			</tr>
		</c:forEach>
	</table>
	<c:if test="${empty frd.values}">
		<script>
			$(function() {
				document.getElementById('${addButtonUiid}').click();
			})
		</script>
	</c:if>
</u:sc>