<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<form action="${form.requestURI}">
	<c:if test="${ctxUser.checkPerm('/admin/plugin/dba/query/history:get') and not empty frd.storedQueries}">
		<div class="mb1" style="white-space: nowrap;">
			<button type="button" class="btn-grey icon mr1" title="${l.l('Load query')}" onclick="$$.dba.queryHistoryGet(this)"><i class="ti-import"></i></button><%--
		--%><ui:combo-single hiddenName="queryHistoryId" list="${frd.storedQueries}" styleClass="layout-width-rest"/>
			<p:check action="/admin/plugin/dba/query/history:del">
				<button type="button" class="btn-grey icon ml1" title="${l.l('Delete from history')}" onclick="if ($$.confirm.del()) { $$.dba.queryHistoryDel(this) }"><i class="ti-trash"></i></button>
			</p:check>
		</div>
	</c:if>

	<c:set var="uiid" value="${u:uiid()}"/>
	<textarea id="${uiid}" name="query" class="w100p" rows="20">${form.param.query}</textarea>

	<div class="mt1">
		<ui:button type="out" onclick="$$.ajax.loadContent(this)"/>
		<ui:page-control nextCommand="; $$.ajax.loadContent(this)"/>
	</div>

	<script>
		$$.ui.codeMirror('${uiid}', 'text/x-mysql');
	</script>
</form>

<c:set var="table" value="${frd.table}"/>
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

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>