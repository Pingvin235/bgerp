<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div style="display:table; width: 100%;" id="${uiid}" class="in-table-row">
	<div class="in-table-cell in-pl1 in-pb05">
		<div style="width: 100%;">
			<u:sc>
				<c:set var="additionalSourceFilter">
					var selectedValues = {};
					$('#${uiid} input[name=value]').each(function()
					{
						selectedValues[$(this).attr('itemId')] = 1;							
					});
					
					var filteredSourceWithoutSelected = [];
					for( var i = 0; i < filteredSource.length; i++ )
					{
						if( !selectedValues[filteredSource[i].id] )
						{
							filteredSourceWithoutSelected.push( filteredSource[i] );
						}
					}
					
					filteredSource = filteredSourceWithoutSelected;
				</c:set>
				
				<c:set var="onSelect">
					$('#${uiid}')
						.data('id', $hidden.val())
						.data('title',  $input.val());										
				</c:set>
				
				<c:remove var="id"/>
				<c:set var="style" value="width: 100%;"/>
				<c:set var="hiddenName" value="${uiid}-addingValue"/>
				<%@ include file="/WEB-INF/jspf/select_single.jsp"%>
			</u:sc>
		</div>
		<div>
			<input name="count" size="2" onkeydown="return isNumberKey(event)"/>
		</div>
		<div>
			<button class="btn-green">+</button>
			
			<script>
				$(function(){
					$('#${uiid} button.btn-green').click(function()
					{
						var $editor = $('#${uiid}');
					
						var id = $editor.data('id');
						var title = $editor.data('title');
						var count = $editor.find('input[name=count]').val();
					
						if (!id) 
						{
							alert('Не выбран элемент');
							return;
						}
						
						if (!count) 
						{
							alert('Не указано количество');
							return;
						}
						
						$editor.append( 
								sprintf("<div class=\'in-table-cell in-pl1 in-pt05\'>\
											<div>\
												<input type=\'hidden\' name=\'value\' itemId=\'%s\' value=\'%s\'/>\
												%s\
											</div>\
											<div><input type=\'text\' value=\'%s\' size=\'2\' onkeydown=\'return isNumberKey(event)\'\
											onchange=\"$(this).parent().parent().find('input[name=value]').val('%s:' + this.value)\"/></div>\
											<div><button class=\'btn-white\' onclick=\'$(this).parent().parent().remove();\'>X</button></div>\
										</div>", id, id + ':' + count, title, count, id ) );
						
						$editor.removeData('id').removeData('title');
					});
				})
			</script>
		</div>
	</div>
	
	<c:forEach var="item" items="${listValues}">
		<c:set var="count" value="${values[item.id]}"/>
		<c:if test="${not empty count}">
			<div class="in-table-cell in-pl1 in-pt05">
				<div>
					<input type="hidden" name="value" itemId="${item.id}" value="${item.id}:${count.count}"/>
					${item.title}
				</div>
				<div><input type="text" value="${count.count.stripTrailingZeros().toPlainString()}" size="2" onkeydown="return isNumberKey(event)" onchange="$(this).parent().parent().find('input[name=value]').val('${item.id}:' + this.value)"/></div>
				<div><button class="btn-white" onclick="$(this).parent().parent().remove();">X</button></div>
			</div>
		</c:if>
	</c:forEach>		
</div>
<div class="hint">Вы можете использовать точку как указатель десятичной дроби.</div>	