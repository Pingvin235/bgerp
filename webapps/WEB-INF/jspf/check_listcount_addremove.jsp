<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
list - перечень элементов
paramName - имя input параметра
available - ограничение на перечень
values - выбранные значения
addToInput - добавить в тег input атрибуты
showId - 1, отображать ID элементов 
config - мап с конфигурацией параметра

afterAddCommand
afterDelCommand
beforeAddCommand
beforeDelCommand
--%>

<c:set var="uiid" value="${u:uiid()}"/>
<div style="height:30px;">
<select id="${uiid}select" class="parametersSelect" style="min-width:200px;max-width:200px; width:200px">
	<c:forEach var="itemId" items="${list.keySet()}">
		<c:if test="${not u:contains( values.keySet(), itemId )}">
			 <option value=${itemId}>${list[itemId]}</option>
		</c:if>
	</c:forEach>
</select>

<input type="text" value="1.0" style="width:45px" onkeydown="return isNumberKey(event);" />
<input type="button" value="Добавить" style="position: relative;" 
	onclick="${beforeAddCommand}; 
			 var selected = $('#${uiid}select').val();
			 addParameter('${uiid}',$(this).prev().val(),true);
			 $('select#${uiid}select').next().children().val($('select#${uiid}select').children('option:selected').text());
			 $('#${uiid}row' + selected).find('input[type=checkbox][name=comment]').val('');
			 ${afterAddCommand}"/>
</div>
</br>
<div id="${uiid}tableDiv" style="min-height:150px;overflow:auto;">
	<table style="width:100%;" id="${uiid}table">
		<c:forEach var="itemId" items="${list.keySet()}">
			<c:if test="${(empty available or u:contains( available, itemId)) and u:contains( values.keySet(), itemId ) }">
				<%@ include file="check_listcount_addremove_item.jsp"%>
			</c:if>
		</c:forEach>
	
		<c:forEach var="itemId" items="${list.keySet()}">
			<c:if test="${(empty available or u:contains( available, itemId)) and not u:contains( values.keySet(), itemId )  }">
				<%@ include file="check_listcount_addremove_item.jsp"%>
			</c:if>
		</c:forEach>
	</table>
</div>

<script>
	$('select#${uiid}select').combobox();	
    
	normalizeDivHeight('${uiid}');
</script>

<c:set var="addToInput" value=""/>
<c:set var="showId" value=""/>
