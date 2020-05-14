<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<table style="width: 100%;" id="${uiid}">
	<tr><td>
		<c:set var="pageFormSelectorFunc" value="$('#processQueueFilter').find('form#${queue.id}-${form.param.savedFilterSetId}')"/>
		<c:set var="nextCommand">; processQueueMarkFilledFilters(${pageFormSelectorFunc}); $$.ajax.load(${pageFormSelectorFunc}[0], $('#processQueueData'));</c:set> 
		<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
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
				bgcrm.debug( 'processQueue', 'refresh queue', $("#${uiid} button[name='pageControlRefreshButton']") );
				addProcessQueueIdToUrl( ${queue.id} );
			});
			
		bgcrm.debug( 'processQueue', 'added onShow callback on ', $contentDiv );
	});
</script>


<%@ include file="/WEB-INF/jspf/user/process/queue/show_table.jsp"%>

<table style="width: 100%;">
	<tr><td>
		<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
	</td></tr>
</table> 