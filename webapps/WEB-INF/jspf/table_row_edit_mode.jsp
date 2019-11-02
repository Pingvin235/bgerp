<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="editButton" value="${true}"/>	
		
<c:if test="${u:getFromPers( ctxUser, 'iface.table.rowHighlightClickEdit', '1' ) eq 1}">
	 <c:set var="editButton" value="${false}"/>
	 
	 <script>
	 	$(function()
	 	{
	 		var $dataTable = $('#${uiid}');
	 		
	 		tableRowHl( $dataTable, ${rows} );
	 		
			var callback = function( $clicked )
			{
				var $row = $clicked;
				
				var openUrl = $row.attr( 'openUrl' );
				var openCommand = $row.attr( 'openCommand' );
				
				if( openUrl )
				{
					openUrlToParent( openUrl, $('#${uiid}') )
				}
				else if( openCommand )
				{
					eval( openCommand );
				}
			}; 
			
			doOnClick( $dataTable, 'tr:gt(0)', callback );		
	 	});
	 </script>		
</c:if>