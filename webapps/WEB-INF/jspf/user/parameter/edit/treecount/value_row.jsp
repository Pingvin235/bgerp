<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Incoming variables:
	treeRootNode - tree root node
	multiple     - multiple values
Optional for a value:
	itemId       - id
	itemTitle    - title
	itemCount    - count
--%>

<tr>
	<td>
		<a href="#" onclick="$$.param.treecount.treeOpen(this); return false;"><c:out value="${itemTitle}" default="undefined"/></a>
		<div style="display: none;">
			<ui:tree-single rootNode="${treeRootNode}" name="itemId" nameTitle="itemTitle" value="${itemId}" style="height: 20em; overflow: auto;"/>
			<ui:button type="ok" styleClass="mt1 btn-white" onclick="$$.param.treecount.treeClose(this, 'itemTitle'); return false;"/>
		</div>
	</td>
	<td>
		<ui:input-decimal name="itemCount" value="${itemCount}" digits="2" size="4" title="${l.l('Use dot as a decimal separator')}"/>
	</td>
	<td>
		<button type="button" class="btn-white btn-small icon" onclick="$$.param.treecount.delValue(this, ${multiple})"><i class='ti-trash'></i></button>
	</td>
</tr>