<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="refreshForm" value="openUrlTo( formUrl( $('#fiasHouse') ), $('#fiasHouse').next() );"/>

<form action="plugin/fias.do">
	<input type="hidden" name="action" value="copyPostalCode"/>
	<input type="hidden" name="streetId" value=""/>
	
	<input type="button" value="Скопировать индексы для выделеных" 
		onclick="if(sendAJAXCommand(formUrl(this.form))) { alert('Индексы скопированны'); ${refreshForm}}"/>
	<input type="button" value="Скопировать индексы для всех" 
		onclick="$(this.form).find('input[name=streetId]').val($('#fiasHouse select[name=streetId]').find('option:selected').val());
				 if(sendAJAXCommand(formUrl(this.form))) { alert('Индексы скопированны');${refreshForm}}"/>
	
	<%--промотчик для пред формы --%>
	<c:set var="pageFormId" value="fiasHouse"/>
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
	
	<table class="data" width="100%">
		<tr>
			<td></td>
			<td></td>
			<td>Дом</td>
			<td>Почтовый индекс (ФИАС)</td>
			<td>Почтовый индекс (Адресный справочник)</td>
		</tr>
		<c:forEach var="house" items="${form.response.data.list}">
			<tr>
				<td align="center" width="30px">
					<c:url var="deleteAjaxUrl" value="plugin/fias.do">
						<c:param name="action" value="delHouseLink"/>
						<c:param name="houseId" value="${house.id}"/>
					</c:url>
					<c:set var="deleteAjaxCommandAfter" value="${refreshForm}"/>
					<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				</td>
				<td align="center" width="30px">
					<input type="checkbox" name="houseId" value="${house.id}"/>
				</td>
				<td align="center">${house.houseNum} ${house.houseFrac}</td>
				<td align="center">${house.fiasPostalCode}</td>
				<td align="center">${house.crmPostalCode}</td>
			</tr>
		</c:forEach>
	</table>
</form>