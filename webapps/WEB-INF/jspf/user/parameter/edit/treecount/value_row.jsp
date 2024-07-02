<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	multiple or form.param.multiple
	itemId or form.param.itemId
	itemTitle or form.param.itemTitle
	itemCount or form.param.itemCount
--%>

<tr>
	<td>
		<a href="#" onclick="$$.param.treecount.treeOpen(this); return false;">${not empty itemTitle ? itemTitle : form.param.itemTitle}</a>
		<div style="display: none;">
			<ui:tree-single rootNode="${treeRootNode}" hiddenName="itemId" hiddenNameTitle="itemTitle" value="${not empty itemId ? itemId : form.param.itemId}"
				style="height: 20em; overflow: auto;"/>
			<ui:button type="ok" styleClass="mt1 btn-white" onclick="$$.param.treecount.treeClose(this, 'itemTitle'); return false;"/>
		</div>
	</td>
	<td>
		<input type="text" name="itemCount" value="${not empty itemCount ? itemCount : form.param.itemCount}" size="4"
			onkeydown="return isNumberKey(event)" title="${l.l('Use dot as a decimal separator')}"/>
	</td>
	<td>
		<button class="btn-white icon" onclick="$(this).closest('tr').remove(); $$.param.treecount.editorToggle(${not empty multiple ? multiple : form.param.multiple}, this);"><i class='ti-trash'></i></button>
	</td>
</tr>