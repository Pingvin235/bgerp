<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="queue" value="${form.response.data.queue}"/>

<c:set var="key" value="queueCurrentSavedFilterSet.${queue.id}"/>
<c:set var="currentSavedFilterSetId" value="${ctxUser.personalizationMap[key]}"/>

<c:set var="config" value="${u:getConfig( ctxUser.personalizationMap, 'ru.bgcrm.model.process.queue.config.SavedFiltersConfig' )}"/>
<c:set var="currentSavedFilterSet" value="${config.savedFilterSetMap[u:int( currentSavedFilterSetId )]}"/>

<span id="${queue.id}" style="width: 100%;">
	<table style="width: 100%;"><tr>
		<td nowrap="nowrap">
			<%-- TODO: приделать проверку прав --%>
			<c:if test="${queue.configMap['allowCreateProcess'] ne 0}">
				<input type="button" value="Создать" onclick="$('#processQueueShow').hide(); $('#processQueueCreateProcess').show();"/>
			</c:if>	
			<input type="button" value="Применить" onclick="openUrlTo( formUrl( $('#processQueueFilter').find('form#' + ${queue.id} + '-' + $('#filterSet input[checked]')[0].value ) ), $('#processQueueData') );"/>
			<c:if test="${ not empty queue.configMap['allowPrint']}">
				<input type="button" value="Печать" onclick="window.location.href = formUrl( $('#processQueueFilter').find('form#' + ${queue.id} + '-' + $('#filterSet input[checked]')[0].value )) +'&print=1'"/>
			</c:if>	
		</td>	
		<td nowrap="nowrap" id="filterSet">
			Сохранённые фильтры:
			<c:set var="uiid" value="${u:uiid()}"/>
			
			<c:if test="${ empty currentSavedFilterSet}">
				<c:set var="checked">checked="checked"</c:set>
			</c:if>
			
		    <input type="radio" name="queueFilterSet" value="0" style="display: none;" 
		        		id="${uiid}" ${checked}/>
		    <label for="${uiid}">Полный</label>
		        				    
		    
			<c:forEach var="item" items="${config.queueSavedFilterSetsMap[queue.id]}">
				<c:set var="uiid" value="${u:uiid()}"/>
				
				<c:remove var="checked"/>
				<c:if test="${currentSavedFilterSetId eq item.id}">
					<c:set var="checked">checked="checked"</c:set>
				</c:if>
				
				<input type="radio" name="queueFilterSet" value="${item.id}" style="display: none;" id="${uiid}" ${checked}/>
				<label for="${uiid}">${item.title}</label>				
			</c:forEach>
		</td>
    
	    <c:set var="uiid" value="${u:uiid()}"/>
	    <td nowrap="nowrap" id="${uiid}" width="100%">
	    	<span id="buttons">
	    		<input type="button" value="+" title="Сохранить фильтры в набор" onclick="$('#${uiid} #buttons').hide(); $('#${uiid} #editor').show(); "/>
	    		<input type="button" value="X" 
	    			title="Удалить выбранный набор фильтров" 
	    			onclick="if( !confirm( 'Удалить выбранный набор?' ) ){ return; }
	    					
	    					savedSetId = $('#processQueueButtons').find( 'span#${queue.id}' ).find( 'input[checked=checked]' ).val();
					    	if( savedSetId <= 0 )
					    	{
					    		alert( 'Невозможно удалить полный фильтр' );
					    		return;
					    	}
					    	
					    	if( sendAJAXCommand( 'process.do?action=queueSavedFilterSet&queueId=${queue.id}&id=' + savedSetId + '&command=delete' ) ){ processQueueFilterSetSelect( ${queue.id} ) }"/>
	    	</span>
	    	<span>
		    	<form action="process.do" id="editor" style="display: none;">
		    		Название:    	
		   			<input type="hidden" name="action" value="queueSavedFilterSet"/>
		   			<input type="hidden" name="queueId" value="${queue.id}"/>
		   			<input type="hidden" name="command" value="add"/>
		   			<input type="hidden" name="url"/>
		   			<input type="text" name="title" size="20"/>
		   			
		   			<input type="button" value="OK" 
		   				onclick="if( this.form.title.value == '' ){ alert( 'Введите название!'); return; } 
		   						 this.form.url.value = formUrl( $('#processQueueFilter').find('form#${queue.id}-0'), ['page.pageIndex', 'savedFilterSetId'] );
		   						 if( sendAJAXCommand( formUrl( this.form ) ) ){ processQueueFilterSetSelect( ${queue.id} ) }"/> 
		   			<input type="button" value="Отмена" onclick="$('#${uiid} #buttons' ).show(); $(this.form).hide()"/>
		   		</form>
	   		</span>
	    </td>
	</tr></table>    
    
    <script>
	    $(function() {
	    	var $filterSet = $("#processQueueButtons").find( "span#${queue.id}" ).find( "td#filterSet" );

	    	$filterSet.buttonset();
	        
	        $filterSet.find( "input[name='queueFilterSet']" ).change( function(event) {
	        	processQueueChanged( this.value );
	    		
	    		$("#processQueueButtons").find( "span#${queue.id}" ).find( "input[type=radio]" ).removeAttr( "checked" );	    		
	    		this.setAttribute( "checked", "checked" ); 
          	});
	    });
    </script> 
</span>	
