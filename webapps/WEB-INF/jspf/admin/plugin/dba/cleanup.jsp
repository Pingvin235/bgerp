<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<h2>Inconsistencies Cleanup Queries</h2>

	<form action="/admin/plugin/dba/cleanup.do">
		<input type="hidden" name="action" value="queryRun"/>

		<table class="data hl mb1">
			<tr>
				<td><input type="checkbox" onclick="$(this).closest('table').find('input[name=query]').prop('checked', this.checked)"/></td>
				<td width="100%">Query</td>
				<td>Rows</td>
			</tr>
			<c:forEach var="item" items="${form.response.data.inconsistencyCleanupQueries}">
				<c:set var="uiid" value="${u:uiid()}"/>
				<tr>
					<td><input type="checkbox" name="query" value="${item}"/></td>
					<td>${item}</td>
					<td id="${uiid}"><%-- content loaded dynamically here --%></td>
				</tr>
				<c:url var="dryRunUrl" value="/admin/plugin/dba/cleanup.do">
					<c:param name="action" value="queryDryRun"/>
					<c:param name="query" value="${item}"/>
				</c:url>
				<script>
					$$.ajax.load('${dryRunUrl}', $('#${uiid}'));
				</script>
			</c:forEach>
		</table>

		<button type="button" class="btn-grey" onclick="$$.ajax.post(this, {control: this}).done(() => $$.ajax.loadContent('${form.requestUrl}', this));">Execute</button>
		<button type="button" class="btn-white ml1" onclick="$$.ajax.loadContent('${form.returnUrl}', this)">Close</button>
	</form>
</div>

<shell:title text="Database"/>
<shell:state text="Cleanup"/>
