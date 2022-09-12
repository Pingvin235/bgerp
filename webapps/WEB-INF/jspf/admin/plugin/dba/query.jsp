<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<form action="/admin/plugin/dba/query.do">
	<c:if test="${ctxUser.checkPerm('org.bgerp.plugin.svc.dba.action.admin.QueryHistoryAction:get') and not empty form.response.data.storedQueries}">
		<div class="mb1 in-mr1" style="display: flex;">
			<button type="button" class="btn-grey icon" title="${l.l('Load query')}" onclick="$$.dba.queryHistoryGet(this)"><i class="ti-import"></i></button>
			<ui:combo-single hiddenName="queryHistoryId" list="${form.response.data.storedQueries}" style="width: 100%;"/>
			<p:check action="org.bgerp.plugin.svc.dba.action.admin.QueryHistoryAction:del">
				<button type="button" class="btn-grey icon" title="${l.l('Delete from history')}" onclick="if ($$.confirm.del()) { $$.dba.queryHistoryDel(this) }"><i class="ti-trash"></i></button>
			</p:check>
		</div>
	</c:if>

	<c:set var="uiid" value="${u:uiid()}"/>
	<div style="height: 20em;">
		<textarea id="${uiid}" name="query" class="w100p">${form.param.query}</textarea>
	</div>

	<%-- for some reason this produces one em top indent --%>
	<div class="mt2">
		<ui:button type="out" onclick="$$.ajax.loadContent(this)"/>
		<ui:page-control nextCommand="; $$.ajax.loadContent(this)"/>
	</div>

	<script>
		$$.ui.codeMirror('${uiid}', 'text/x-mysql');
	</script>
</form>

<c:set var="table" value="${form.response.data.table}"/>
<c:if test="${not empty table}">
	<table class="data mt1 hl">
		<tr>
			<c:forEach var="col" items="${table.columns}">
				<td>${col}</td>
			</c:forEach>
		</tr>
		<c:forEach var="row" items="${table.list}">
			<tr>
				<c:forEach var="cell" items="${row}">
					<td>${cell}</td>
				</c:forEach>
			</tr>
		</c:forEach>
	</table>
</c:if>

<shell:title text="${l.l('SQL Query')}"/>
