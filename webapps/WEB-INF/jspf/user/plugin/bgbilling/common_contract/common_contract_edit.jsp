<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script type="text/javascript">
	$(function()
	{
		$("#dateFrom").datepicker();
		$("#dateTo").datepicker();
	});
</script>

<c:set var="contract" value="${form.response.data.contract}"/>

<c:if test="${not empty contract}">
	<c:set var="uiid" value="${u:uiid()}"/>

	<%--
	<table style="width: 100%;" id="${uiid}">
		<tr>
			<td class="title" nowrap="nowrap" colspan="3">ID:${contract.id} ${contract.formatedNumber}</td>
		</tr>
		<tr>				
			<td class="box" nowrap="nowrap">
				Контрагент: <a href="#UNDEF" onclick="openCustomer( ${customer.id} ); return false;">${customer.title}</a>
			</td>
			<td class="box" nowrap="nowrap">
				Адрес: ${contract.address.value}
			</td>
			<td class="box" nowrap="nowrap">
				Период: <fmt:formatDate value="${contract.dateFrom}" pattern="dd.MM.yyyy"/> - <fmt:formatDate value="${contract.dateTo}" pattern="dd.MM.yyyy"/>
			</td>
		</tr>
	</table>
	 --%>
	
	<table style="width: 100%;" id="${uiid}">
		<tr>
			<td><h2>Редактирование Единого договора</h2></td>
		</tr>
	</table>
	
	<form action="plugin/bgbilling/commonContract.do">
		<input type="hidden" name="action" value="commonContractUpdate"/>
		<input type="hidden" name="id" value="${contract.id}"/>
		<table class="data" style="width:100%;">
		<tr>
			<td width="200">Параметр</td>
			<td>Значение</td>
		</tr>
		<tr>
			<td>Номер</td>
			<td><input type="text" value="${contract.areaId}" disabled="disabled" style="width:30px;text-align:center;"><input name="number" type="text" value="${contract.number}"/></td>
		</tr>
		<tr>
			<td>Дата открытия</td>
			<td><input name="dateFrom" size="10" type="text" id="dateFrom" value="<fmt:formatDate value="${contract.dateFrom}" pattern="dd.MM.yyyy"/>"/></td>
		</tr>
		<tr>
			<td>Дата закрытия</td>
			<td><input name="dateTo" size="10" type="text" id="dateTo" value="<fmt:formatDate value="${contract.dateTo}" pattern="dd.MM.yyyy"/>"/></td>
		</tr>
		<tr>
			<td>Пароль</td>
			<td><input name="password" size="10" type="text" value="${contract.password}"/></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>
				<c:url var="returnUrl" value="plugin/bgbilling/commonContract.do">
					<c:param name="id" value="${contract.id}"/>
				</c:url>
			
				<button type="button" class="btn-grey" onclick="if( sendAJAXCommand( formUrl( this.form ) ) ){ openUrlToParent( '${returnUrl}', $('#${uiid}') ) }">ОК</button> 
				<button type="button" class="btn-grey" onclick="openUrlToParent( '${returnUrl}', $('#${uiid}') )">${l.l('Отмена')}</button>
			</td>
		</tr>		
		</table>
	</form>
</c:if>