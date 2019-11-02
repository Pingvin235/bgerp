<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div width="100%">
	<form action="plugin/fias.do">
		<input type="hidden" name="action" value="addStreetLink"/>
			
		<b>Фильтр по городам:</b>
		</br>
		<select name="cityId" onChange="$('#linkStreetForm').find('input[name=cityId]').val($('select[name=cityId]').find('option:selected').val());fias_clearStreetInput();">
			<c:forEach	var="city" items="${form.response.data.cityList}">
				<option value="${city.id}">${city.title}</option>
			</c:forEach>
		</select>

		<h2>Список не привязанных:</h2>

		<table width="100%" id="fias-sync">
			<tr>
				<td width="50%">
					<b>Адресный справочник:</b></br>
					<input type="text" style="width:100%" name="crmStreet" value="" onkeyup="fias_searchAddressStreetByTerm()"/>
					</br>
					<select multiple="multiple" size="10" style="width:100%" name="crmStreetId"
						 onChange="fias_searchSimilarStreet($(this).find('option:selected').text())">
					</select>
				</td>
				<td width="50%">
					<b>Справочник ФИАС:</b></br>
					<input type="text" style="width:100%" name="fiasStreet" value="" onkeyup="fias_searchFiasStreetByTerm()"/>
					</br>
					<select multiple="multiple" size="10" style="width:100%" name="fiasStreetId">
					</select>
				</td>
			</tr>
			<tr>
				<td colspan="2" align="center">
					<input type="button" value="Привязать" onclick="if( confirm( 'Вы уверены, что хотите привязать?' ) && sendAJAXCommand(formUrl(this.form))) {alert('Привязка произведена успешно');fias_clearStreetInput();}"/> 
				</td>
			</tr>
		</table>
	</form>
</div>

<div width="100%">
	<form action="plugin/fias.do" id="linkStreetForm" onsubmit="return false;">
		<h2>Список привязанных</h2>
		<input type="hidden" name="action" value="linkStreetList"/>
		<input type="hidden" name="cityId" value="${form.response.data.cityList.get(0).id}"/>
		<input type="text" style="width:100%" name="titleTerm" value="" onkeyup="openUrlTo(formUrl(this.form),$(this.form).next())"/>
	</form>
	<div>
	</div>
</div>