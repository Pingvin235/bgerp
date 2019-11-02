<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<table style="width: 100%;" id="${uiid}">
	<tr><td>
		<c:set var="pageFormSelectorFunc" value="$('#processQueueFilter').find('form#${queue.id}-${form.param.savedFilterSetId}')"/>
		<c:set var="nextCommand">; processQueueMarkFilledFilters(${pageFormSelectorFunc}); openUrlTo( formUrl( ${pageFormSelectorFunc}[0] ), $('#processQueueData') );</c:set> 
		<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
	</td></tr>
</table>

<%-- обновление очереди по переходу в неё --%>
<script>
	$(function()
	{
		var $contentDiv = $('#content > #processQueue');
		
		// т.к. каждый раз UIID промотчика страниц разный - переопределение onShow
		$contentDiv.data('onShow', 
			function()
			{
				$("#${uiid} button[name='pageControlRefreshButton']").click();
				bgcrm.debug( 'processQueue', 'refresh queue', $("#${uiid} button[name='pageControlRefreshButton']") );
				addProcessQueueIdToUrl( ${queue.id} );
			});
			
		bgcrm.debug( 'processQueue', 'added onShow callback on ', $contentDiv );
		
		var $dataTable = $('#content > #processQueue #processQueueData > table.data');
		
		<c:if test="${u:getFromPers( ctxUser, 'iface.processQueue.rowHighlight', '1' ) eq 1}">
			tableRowHl( $dataTable );
		</c:if>	
		
		<c:if test="${u:getFromPers( ctxUser, 'iface.processQueue.openOnClick', '1' ) eq 1}">
			var callback = function( $clicked )
			{
				var $row = $clicked;
				
				var processId = $row.attr( 'processId' );
				if( processId )
				{
					openProcess( processId );	
				}
				else
				{
					alert( 'Не найден атрибут строки processId!' );
				}
			}; 
			
			doOnClick( $dataTable, 'tr:gt(0)', callback );		
		</c:if>	
	});
</script>

<%@ include file="/WEB-INF/jspf/user/process/queue/show_table.jsp"%>

<table style="width: 100%;">
	<tr><td>
		<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
	</td></tr>
</table> 