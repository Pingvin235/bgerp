<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="pb1" style="display: flex;">
	<div class="w100p"><%-- processors --%></div>
	<div>${l.l('Записей')}:&nbsp;${form.response.data.page.recordCount}</div>
</div>
<div>
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