<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="paramName" value="executor"/>
<c:if test="${not empty form.param.paramName}">
	<c:set var="paramName" value="${form.param.paramName}"/>
</c:if>

<%-- <ul style="width: 100%;"> --%>
	<li>
		<input type="checkbox" name="${paramName}" value="empty" ${u:checkedFromCollection( form.getParamArray( 'executor' ), 'empty' )}/>
		<span>** Не указан **</span>
	</li>
	<li>
		<input type="checkbox" name="${paramName}" value="current" ${u:checkedFromCollection( form.getParamArray( 'executor' ), 'current' )}/>
		<span>** Я **</span>
	</li>
	<c:forEach var="item" items="${form.response.data.list}">
		<li>
			<input type="checkbox" name="${paramName}" value="${item.id}" ${u:checkedFromCollection( form.getParamArray( 'executor' ), item.id )}/>
			<span>${item.title}</span>
		</li>
	</c:forEach>
	
	<%--
	<script>
		$(function()
		{	
			var $comboDiv = $('#${uiid}');
			var $drop = $comboDiv.find('ul.drop');
			
			var updateCurrentTitle = function()
			{
				var checkedCount = 0;
				var titles = "";
				
				$comboDiv.find( "ul.drop li input" ).each( function()
				{
					if( this.checked )				
					{
						checkedCount++;
						var title = $(this).next().text();
						if( titles.length > 0 )
						{
							titles += ", ";
						}
						titles += title;
					}
				});
				
				$comboDiv.find( '.text-value' ).text( "[" + checkedCount + "] " + titles );
				
				${onChange}
			};
			
			$comboDiv.find( "ul.drop li input" ).click( function( event ) 
			{
				updateCurrentTitle();
				event.stopPropagation();
				
				//console.log( "1" + event );
			});
			
			$comboDiv.find( "ul.drop li" ).each( function()
			{
				var li = this;
				$(li).click( function( event ) 
				{
					$(li).find( "input" ).click();
				});
			})		
			
			$comboDiv.click( function() 
			{
				$drop.show();
				
				$(document).one( "click", function() {
					$drop.hide();
				});
					 
				return false;
			});
			
			$comboDiv.find( "div.icon" ).click( function( event )
			{
				$comboDiv.find( "ul.drop li input" ).each( function()
				{
					this.checked = false;				
				});
				updateCurrentTitle();
				
				event.stopPropagation();
			});		
			
			updateCurrentTitle();
		})
	</script>
	--%>
<%-- </ul>  --%>