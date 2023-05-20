<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	listValues - available list values
	values     - current values
	multiple   - multiple values
--%>

<c:choose>
	<c:when test="${multiple}">
		<c:set var="uiid" value="${u:uiid()}"/>
		<table id="${uiid}" class="data">
			<tr>
				<td width="100%">${l.l('Title')}</td>
				<td>${l.l('Amount')}</td>
				<td>&nbsp;</td>
			</tr>
			<c:forEach var="item" items="${listValues}">
				<c:set var="value" value="${values[item.id]}"/>
				<c:if test="${not empty value}">
					<u:sc>
						<c:set var="itemId" value="${item.id}"/>
						<c:set var="itemTitle" value="${item.title}"/>
						<c:set var="itemCount" value="${value.count.stripTrailingZeros().toPlainString()}"/>
						<%@ include file="value_row.jsp"%>
					</u:sc>
				</c:if>
			</c:forEach>
			<%-- editor --%>
			<tr>
				<td>
					<ui:select-single list="${listValues}" hiddenName="newItemId" onSelect="this.form.newItemTitle.value = $input.val()" styleClass="w100p"/>
					<input type="hidden" name="newItemTitle"/>
				</td>
				<td>
					<input name="newItemCount" size="2" onkeydown="return isNumberKey(event)"/>
				</td>
				<td>
					<ui:button type="add" onclick="$$.param.listcount.addValue(
						$('#${uiid}'), [
							'${l.l('No value choosen')}',
							'${l.l('No quantity defined')}'
						]
					)"/>
				</td>
			</tr>
		</table>
	</c:when>
	<c:otherwise>
		<div style="display: flex;">
			<ui:select-single hiddenName="itemId" list="${listValues}" value="${u.getFirst(values.keySet())}"  styleClass="w100p"/>
			<input name="itemCount" value="${u.getFirst(values.entrySet()).value.count.stripTrailingZeros().toPlainString()}" size="2" onkeydown="return isNumberKey(event)" class="ml1"/>
		</div>
	</c:otherwise>
</c:choose>
