<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script>addCustomCitySearch('#${paramName}City','#${paramName}CityId');</script>

Город:
</br> 
<input type="text" style="width:100%" id="${paramName}City" onkeypress="if( enterPressed( event ) ){ ${sendCommand} }"
	onkeyup="if(this.form.${paramName}City.value=='') this.form.${paramName}CityId.value='';"/></br>
Квартал:
</br>
<input type="text" style="width:100%" id="${paramName}Quarter" onkeypress="if( enterPressed( event ) ){ ${sendCommand} }"
	onkeyup="if(this.form.${paramName}Quarter.value=='') this.form.${paramName}QuarterId.value='';"/>

<input type="hidden" id="${paramName}CityId" 
	onchange="addCustomQuarterSearch('#${paramName}Quarter','#${paramName}QuarterId',this.form.${paramName}CityId.value);"/>
<input type="hidden" id="${paramName}QuarterId"/>

</br>

<input type="button" value="Добавить" 
	onclick="addQuarterItem('#${paramName}filterList','${paramName}',this.form.${paramName}QuarterId.value,this.form.${paramName}City.value+' ' +this.form.${paramName}Quarter.value);"/>

<table id="${paramName}filterList">
	<!-- сюда загрузятся выбранныее кварталы -->
</table>
			 
<script>
	function addQuarterItem(selector,name,valueId,valueText) { 
		    var appendString = "<tr style='width: 100%;'>";
		    appendString+="<td style='display:none' align='center'>";
		    appendString+="<input type='checkbox' name='"+name+"' value='"+valueId+"' checked='checked'/></td>";			
		    appendString+="<td nowrap='nowrap' style='padding-left: 0px; padding-top: 5px;'>";
		    appendString+="<input type='button' title='Удалить' value='X' onclick='$(this).parent().parent().remove();'/>&nbsp;"+valueText+"</td>";
		    appendString+="</tr>";
		    
		    $(selector).append(appendString);
		}
</script>