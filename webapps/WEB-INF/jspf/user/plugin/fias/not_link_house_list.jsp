<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<form action="plugin/fias.do">
	<input type="hidden" name="action" value="manualSetPostalCode"/>

	Индекс:
	<input type="text" name="postalCode" value=""/>
	<input type="button" value="Установить индексы для выделенных" 
		onclick="if(sendAJAXCommand(formUrl(this.form))) { alert('Индексы скопированны'); openUrlTo(formUrl($('#fiasHouse')),$('#fiasHouse').next()); }"/>
	
	<c:if test="${not empty form.response.data.postalCodeList}">
		Рекомендуемые индексы:${form.response.data.postalCodeList}
	</c:if>
	<%--промотчик для предыдущей формы --%>
	<c:set var="pageFormId" value="fiasHouse"/>
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
	
	<table class="data" width="100%">
		<tr>
			<td></td>
			<td>Дом</td>
			<td>Коммент</td>
			<td>Почтовый индекс (Адресный справочник)</td>
		</tr>
		<c:forEach var="house" items="${form.response.data.list}">
			<tr>
				<td align="center" width="30px">
					<input type="checkbox" name="houseId" value="${house.id}"/>
				</td>
				<td align="center">${house.number} ${house.frac}</td>
				<td align="center">${house.comment}</td>
				<td align="center">${house.postalCode}</td>
			</tr>
		</c:forEach>
	</table>
</form>