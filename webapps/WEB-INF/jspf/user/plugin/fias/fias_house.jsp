<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<form action="plugin/fias.do" id="fiasHouse" onsubmit="return false;">
	<input type="hidden" name="action" value="searchHouseByTerm"/>
	
	<b>Фильтры:</b>
	</br>
	Город:
	<select name="cityId" onchange="fias_fillLinkStreetList($(this).find('option:selected').val());openUrlTo(formUrl(this.form),$(this.form).next(),true);">
		<c:forEach	var="city" items="${form.response.data.cityList}">
			<option value="${city.id}">${city.title}</option>
		</c:forEach>
	</select>
	
	Улица:
	<select name="streetId" style="width:200px" onchange="openUrlTo(formUrl(this.form),$(this.form).next());">
	</select>
	
	Сторона:
	<select name="streetSide" onchange="openUrlTo(formUrl(this.form),$(this.form).next());">
		<option value="1">четная и нечетная</option>
		<option value="2">только четная</option>
		<option value="3">только нечетная</option>
	</select>
	
	Дом:
	<input type="text" name="houseTerm" value="" onkeyup="if( !enterPressed(event) ) { openUrlTo(formUrl(this.form),$(this.form).next());}"/>
	
	Фильтр:
	<select name="isLink" onchange="openUrlTo(formUrl(this.form),$(this.form).next());">
		<option value="1">Привязанные</option>
		<option value="0">Не привязанные</option>
	</select>
</form>

<div>
</div>

<script>
	$('select[name=streetId]:visible').combobox();	
</script>