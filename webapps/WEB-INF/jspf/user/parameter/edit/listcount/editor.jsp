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

		<c:set var="addCommand">
			$$.param.listcount.addValue(
				$('#${uiid}'), [
					'${l.l('No value chosen')}',
					'${l.l('No quantity defined')}'
				]
			)
		</c:set>

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
						<c:set var="itemCount" value="${u.format(value)}"/>
						<%@ include file="value_row.jsp"%>
					</u:sc>
				</c:if>
			</c:forEach>
			<%-- editor --%>
			<tr>
				<td>
					<ui:select-single list="${listValues}" name="newItemId" onSelect="this.form.newItemTitle.value = $input.val()" styleClass="w100p" inputAttrs="autofocus"/>
					<input type="hidden" name="newItemTitle"/>
				</td>
				<td>
					<input name="newItemCount" size="4" onkeydown="if (enterPressed(event)) { ${addCommand} }; return isNumberKey(event)" title="${l.l('Use dot as a decimal separator')}.&nbsp;${l.l('Press Enter to add a value')}."/>
				</td>
				<td>
					<ui:button type="add" onclick="${addCommand}"/>
				</td>
			</tr>
		</table>
	</c:when>
	<c:otherwise>
		<div style="display: flex;">
			<ui:select-single name="itemId" list="${listValues}" value="${u.getFirst(values.keySet())}" styleClass="w100p" inputAttrs="autofocus"/>
			<input name="itemCount" value="${u.format(u.getFirst(values.entrySet()).value)}" size="4" onkeydown="return isNumberKey(event)" class="ml1"/>
		</div>
	</c:otherwise>
</c:choose>
