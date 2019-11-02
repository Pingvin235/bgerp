<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script>addCustomStreetSearch( 'input[name=${paramName}Street]', 'input[name=${paramName}StreetId]' );</script>
<script>addCustomHouseSearch( 'input[name=${paramName}House]', 'input[name=${paramName}StreetId]', 'input[name=${paramName}HouseId]' );</script>

Улица: <input type="text" name="${paramName}Street" style="width:100%" onkeypress="if( enterPressed( event ) ){ ${sendCommand} }" onkeyup="if(this.form.${paramName}Street.value=='') this.form.${paramName}StreetId.value='';"/><br/>
Дом/дробь: <input type="text" name="${paramName}House" style="width:100%" onkeypress="if( enterPressed( event ) ) { ${sendCommand} }" onkeyup="if(this.form.${paramName}House.value=='') this.form.${paramName}HouseId.value='';"/><br/>
Квартира: <input type="text" name="${paramName}Flat" style="width:100%" onkeypress="if( enterPressed( event ) ){ ${sendCommand} }"/><br/>
<input type="hidden" name="${paramName}StreetId"/>
<input type="hidden" name="${paramName}HouseId"/>

<input type="button" value="X" title="Очистить" 
	onclick="this.form.${paramName}StreetId.value='';
			 this.form.${paramName}HouseId.value='';
			 this.form.${paramName}Street.value='';
			 this.form.${paramName}House.value='';
			 this.form.${paramName}Flat.value=''"/>