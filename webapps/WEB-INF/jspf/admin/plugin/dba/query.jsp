<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<form action="/admin/plugin/dba/query.do">
	<c:set var="uiid" value="${u:uiid()}"/>
	<div style="height: 10em;">
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

<shell:title text="SQL Query"/>
