<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<c:set var="tables" value="${frd.tables}"/>

	<div class="mt1">
		Tables: <b>${tables.size()}</b>
		Rows: <b>${frd.rows}</b>
		Size: <b>${fu.byteCountToDisplaySize(frd.size)}</b>

		<c:url var="url" value="/admin/plugin/dba/cleanup.do">
			<c:param name="returnUrl" value="${form.requestUrl}"/>
		</c:url>
		<p:check action="org.bgerp.plugin.svc.dba.action.admin.CleanupAction:null">
			[<a href="#" onclick="$$.ajax.loadContent('${url}', this); return false;">cleanup</a>]
		</p:check>
	</div>

	<form action="/admin/plugin/dba/db.do">
		<input type="hidden" name="method" value="tableDrop"/>
		<c:set var="dropEnabled" value="${frd.dropCandidateCnt gt 0 and ctxUser.checkPerm('/admin/plugin/dba/db:tableDrop')}"/>

		<table class="data hl mt1">
			<tr>
				<c:if test="${dropEnabled}">
					<td><b>${frd.dropCandidateCnt}</b> to del</td>
				</c:if>
				<td>Name</td>
				<td>Data Size</td>
				<td>Index Size</td>
				<td>Create Time</td>
				<td>Update Time</td>
			</tr>
			<c:forEach var="item" items="${tables}">
				<tr>
					<c:if test="${dropEnabled}">
						<td>
							<c:if test="${item.dropCandidate}">
								<input type="checkbox" name="table" value="${item.name}" checked="true"/>
							</c:if>
						</td>
					</c:if>
					<td>${item.name}</td>
					<td>${fu.byteCountToDisplaySize(item.dataLength)}</td>
					<td>${fu.byteCountToDisplaySize(item.indexLength)}</td>
					<td nowrap="1">${tu.format(item.createTime, 'ymdhms')}</td>
					<td nowrap="1">${tu.format(item.updateTime, 'ymdhms')}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${dropEnabled}">
			<button type="button" class="btn-grey mt1" onclick="
				if (confirm('Do you really want to drop these tables?'))
					$$.ajax.post(this).done(() =>
						$$.ajax.loadContent('${form.requestUrl}', this)
					)
			">Drop selected tables</button>
		</c:if>
	</form>
</div>

<shell:title text="Database"/>
<shell:state text=""/>
