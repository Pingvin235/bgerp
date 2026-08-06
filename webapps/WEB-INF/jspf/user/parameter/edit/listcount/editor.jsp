<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	parameter  - the parameter
	listValues - available list values
	values     - current values
--%>

<c:choose>
	<c:when test="${parameter.multiple}">
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
						<ui:button type="add" id="${addButtonUiid}" styleClass="btn-small" onclick="$$.param.listcount.addValue(this, ${parameter.multiple})" />
					</td>
				</tr>
				<c:forEach var="item" items="${listValues}">
					<c:set var="value" value="${values[item.id]}"/>
					<c:if test="${not empty value}">
						<u:sc>
							<c:set var="itemId" value="${item.id}"/>
							<c:set var="itemCount" value="${u.format(value)}"/>
							<%@ include file="value_row.jsp"%>
						</u:sc>
					</c:if>
				</c:forEach>
			</table>
			<script>
				<c:if test="${empty values}">
					document.getElementById('${addButtonUiid}').click();
				</c:if>
				$$.param.listcount.toggleAddButton(document.getElementById('${addButtonUiid}'), ${parameter.multiple});
			</script>
		</u:sc>
	</c:when>
	<c:otherwise>
		<div style="display: flex;">
			<ui:select-single name="itemId" list="${listValues}" value="${u.getFirst(values.keySet())}" styleClass="w100p" inputAttrs="autofocus"/>
			<ui:input-decimal name="itemCount" value="${u.format(u.getFirst(values.entrySet()).value)}" digits="2" size="4" styleClass="ml1"/>
		</div>
	</c:otherwise>
</c:choose>
