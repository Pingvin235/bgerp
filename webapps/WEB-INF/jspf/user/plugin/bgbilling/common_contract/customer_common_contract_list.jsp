<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/user/plugin/bgbilling/commonContract" styleClass="inTableIndent">
	<input type="hidden" name="action" value="commonContractCreate"/>
	<html:hidden property="customerId"/>
	
	<div style="display: table-cell; width: 100%; white-space: nowrap;">
		<html:select property="addressParamPos" style="width: 100%;">
			<c:forEach var="item" items="${customerAddressMap}">
				<html:option value="${item.key}">${item.value.value}</html:option>
			</c:forEach>
		</html:select>
	</div>
	<div style="display: table-cell;">
		<input type="button" value="Создать" 
			onclick="if( confirm( 'Создать новый единый договор с выбранным адресом?' ) && sendAJAXCommand( formUrl( this.form ) ) ){ openUrlContent( '${form.requestUrl}' ) }"/>
	</div>	
</html:form> 

<table style="width: 100%;" class="data">
	<tr>
		<td>ID</td>
		<td></td>
		<td>Номер</td>
		<td>Период</td>
		<td width="100%">Адрес</td>					
	</tr>
			
	<c:forEach items="${form.response.data.list}" var="contract">
		<tr>
			<td>${contract.id}</td>
			<td>
				<c:set var="formId"/>
				<form action="/user/plugin/bgbilling/commonContract.do">
					<input type="hidden" name="action" value="commonContractDelete"/>
					<input type="hidden" name="id" value="${contract.id}"/>
					<input type="button" value="x" onclick="if( confirm( 'Удалить?' ) ) { sendAJAXCommand( formUrl( this.form ) ); openUrlContent( '${form.requestUrl}' ); }"/>
				</form>
			</td>
			<td><a href="#UNDEF" onclick="bgbilling_openCommonContract(${contract.id}); return false;">${contract.formatedNumber}</a></td>
			<td nowrap="nowrap">${u:formatDate( contract.dateFrom, 'ymd' )} - ${u:formatDate( contract.dateTo, 'ymd' )}</td>	
			<td>${contract.address.value}</td>
		</tr>
	</c:forEach>
</table>