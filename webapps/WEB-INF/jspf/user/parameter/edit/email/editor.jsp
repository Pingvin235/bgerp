<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	frd.value - ParameterPhoneValue
--%>
<u:sc>
	${data.emails.entityAttrEmail}
	<c:set var="addButtonUiid" value="${u:uiid()}"/>
	<table class="data">
		<tr>
			<td width="50%">${l.l('e-mail')}</td>
			<td width="50%">${l.l('Имя')}</td>
			<td><ui:button type="add" id="${addButtonUiid}" styleClass="btn-small" onclick="$$.param.email.addValue(this)"/></td>
		</tr>
		<c:forEach var="item" items="${data.emails.entityAttrEmail.contactList}">
			<tr>
				<td><input type="text" name="address" value="${item.address}" class="w100p"/></td>
				<td><input type="text" name="name" value="${item.name}" class="w100p"/></td>
				<td><button class="btn-white btn-small icon" onclick="$$.param.email.delValue(this)"><i class="ti-trash"></i></button></td>
			</tr>
		</c:forEach>
	</table>
	<c:if test="${empty data.emails.entityAttrEmail.contactList}">
		<script>
			$(function() {
				document.getElementById('${addButtonUiid}').click();
			})
		</script>
	</c:if>
</u:sc>