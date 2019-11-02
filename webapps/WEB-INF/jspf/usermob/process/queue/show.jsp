<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="showEditor">$('#processQueueShow').hide(); $('#processQueueEditProcess').show();</c:set>
<c:set var="editProcessCommand">
	openUrlTo( 'process.do?id=' + PROCESS_ID + '&wizard=1', $('#processQueueEditProcess') ); ${showEditor};
</c:set>

<script>
	$(function()
	{
		var $dataTable = $('#processQueueData > table.data');
		
		var callback = function( $clicked )
		{
			var $row = $clicked;
			
			var processId = $row.attr( 'processId' );
			if( processId )
			{
				${fn:replace(editProcessCommand, 'PROCESS_ID', 'processId')}	
			}
			else
			{
				alert( 'Не найден атрибут строки processId!' );
			}
		}; 
		
		doOnClick( $dataTable, 'tr:gt(0)', callback );
	});
</script>

<c:set var="mob" value="true"/>
<%@ include file="/WEB-INF/jspf/user/process/queue/show_table.jsp"%>