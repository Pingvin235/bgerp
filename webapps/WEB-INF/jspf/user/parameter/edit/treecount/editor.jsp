<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	treeRootNode - tree root node
	values       - current values
	treeValues   - tree parameter values
	multiple     - multiple values
--%>

<c:set var="uiid" value="${u:uiid()}"/>
<table id="${uiid}" class="data">
	<tr>
		<td width="100%">${l.l('Title')}</td>
		<td>${l.l('Amount')}</td>
		<td>&nbsp;</td>
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
	<tr>
		<td>
			<a href="#" onclick="$$.param.treecount.treeOpen(this); return false;">${l.l('undefined')}</a>
			<div style="display: none;">
				<ui:tree-single rootNode="${treeRootNode}" hiddenName="newItemId" hiddenNameTitle="newItemTitle" style="height: 20em; overflow: auto;"/>
				<ui:button type="ok" styleClass="mt1 btn-white" onclick="$$.param.treecount.treeClose(this, 'newItemTitle'); return false;"/>
			</div>
		</td>
		<td>
			<input name="newItemCount" size="4" onkeydown="return isNumberKey(event)"/>
		</td>
		<td>
			<ui:button type="add" onclick="
				$$.param.treecount.addValue(
					${multiple},
					$('#${uiid}'),
					[
						'${l.l('No value choosen')}',
						'${l.l('No quantity defined')}'
					]
				).done(() =>
					$$.param.treecount.editorToggle(${multiple}, this)
				)
			"/>
		</td>
	</tr>
</table>
<script>
	$(function () {
		$$.param.treecount.editorToggle(${multiple}, document.getElementById('${uiid}'));
	})
</script>
