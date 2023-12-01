<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="pb1 in-table-cell">
	<%-- processors --%>
	<div class="w100p in-inline-block">
		<c:forEach var="processor" items="${queue.getProcessors('open')}">
			<c:choose>
				<c:when test="${not empty processor.pageUrl}">
					<c:import url="${processor.pageUrl}?queueId=${queue.id}"/>
				</c:when>
				<c:when test="${not empty processor.jsp}">
					<jsp:include page="${processor.jsp}"/>
				</c:when>
			</c:choose>
		</c:forEach>
	</div>
	<div>${l.l('Records')}:&nbsp;<b>${form.response.data.page.recordCount}</b></div>
</div>
<div id="processQueueData">
	<%@ include file="/WEB-INF/jspf/user/process/queue/show_table.jsp"%>

	<script>
		$(function () {
			const $dataTable = $('#${tableUiid}');

			$$.ui.tableRowHl($dataTable);

			doOnClick($dataTable, 'tr:gt(0)', ($clickedRow) => {
				const processId = $clickedRow.attr('openProcessId');
				if (processId) {
					window.open("/open/process/" + processId);
				}
			});
		});
	</script>
</div>