<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<table style="width: 100%;" id="${uiid}">
	<tr><td>
		<c:set var="pageFormSelector" value="#processQueueFilter form#${queue.id}-${form.param.savedFilterSetId}"/>
		<ui:page-control
			pageFormSelector="${pageFormSelector}"
			nextCommand="; processQueueMarkFilledFilters($('${pageFormSelector}')); $$.ajax.load($('${pageFormSelector}'), $('#processQueueData'));" />
	</td></tr>
</table>

<%-- обновление очереди по переходу в неё --%>
<script>
	$(function()
	{
		var $contentDiv = $('#content > #process-queue');

		// т.к. каждый раз UIID промотчика страниц разный - переопределение onShow
		$contentDiv.data('onShow',
			function()
			{
				$("#${uiid} button[name='pageControlRefreshButton']").click();
				$$.debug( 'processQueue', 'refresh queue', $("#${uiid} button[name='pageControlRefreshButton']") );
				$$.shell.stateFragment(${queue.id});
			});

		$$.debug( 'processQueue', 'added onShow callback on ', $contentDiv );
	});
</script>

<%@ include file="/WEB-INF/jspf/user/process/queue/show_table.jsp"%>

<script>
	$(function () {
		const $dataTable = $('#${tableUiid}');

		$$.ui.tableRowHl($dataTable);

		const callback = function ($clicked) {
			const $row = $clicked;

			const processId = $row.attr('processId');
			if (processId) {
				$$.process.open(processId);
			} else {
				alert('Not found attribute processId!');
			}
		};

		doOnClick($dataTable, 'tr:gt(0)', callback);
	});
</script>

<table style="width: 100%;">
	<tr><td>
		<ui:page-control nextCommand="${nextCommand}" />
	</td></tr>
</table>

<shell:title>
	<jsp:attribute name="text">
		<c:if test="${not empty queue.openUrl}">
			<a target='_blank' href='/open/process/queue/${queue.openUrl}' title='${l.l('Открытый интерфейс')}'>O</a>
		</c:if>
		${l.l('Очереди процессов')}
	</jsp:attribute>
</shell:title>
