<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	parameter    - the parameter
	treeRootNode - tree root node
	values       - current values
	treeValues   - tree parameter values
--%>

<u:sc>
	<table class="data">
		<tr>
			<td width="100%">
				${l.l('Title')}
			</td>
			<td>
				${l.l('Amount')}
			</td>
			<td>
				<c:set var="addButtonUiid" value="${u:uiid()}"/>
				<ui:button type="add" id="${addButtonUiid}" styleClass="btn-small" onclick="$$.param.treecount.addValue(this, ${parameter.multiple})" />
			</td>
		</tr>
		<c:forEach var="item" items="${treeValues}">
			<c:set var="count" value="${values[item.key]}"/>
			<c:if test="${not empty count}">
				<u:sc>
					<c:set var="itemId" value="${item.key}"/>
					<c:set var="itemTitle" value="${item.value}"/>
					<c:set var="itemCount" value="${u.format(count)}"/>
					<%@ include file="value_row.jsp"%>
				</u:sc>
			</c:if>
		</c:forEach>
	</table>
	<script>
		<c:if test="${empty values}">
			document.getElementById('${addButtonUiid}').click();
		</c:if>
		$$.param.treecount.toggleAddButton(document.getElementById('${addButtonUiid}'), ${parameter.multiple});
	</script>
</u:sc>