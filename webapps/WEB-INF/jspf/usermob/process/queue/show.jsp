<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="mob" value="true"/>
<%@ include file="/WEB-INF/jspf/user/process/queue/show_table.jsp"%>

<script>
	$(function () {
		const $dataTable = $('#processQueueData > table.data');
		const callback = function ($row) {
			const processId = $row.attr('processId');
			if (processId) {
				$$.ajax.load('process.do?id=' + processId + '&wizard=1', $('#processQueueEditProcess'));
				$('#processQueueShow').hide(); $('#processQueueEditProcess').show();
			}
			else
				alert('Not found row attribute processId!');
		};

		doOnClick($dataTable, 'tr:gt(0)', callback);
	});
</script>
