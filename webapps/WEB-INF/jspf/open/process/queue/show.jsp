<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="pb1 in-table-cell">
	<%-- processors --%>
	<div class="w100p in-inline-block">
		<c:forEach var="processor" items="${queue.getProcessors('open')}">
			<c:if test="${not empty processor.page}">
				<c:set var="processor" value="${processor}" scope="request"/>
				<jsp:include page="${processor.page}"/>
			</c:if>
		</c:forEach>
	</div>
	<div>${l.l('Записей')}:&nbsp;${form.response.data.page.recordCount}</div>
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