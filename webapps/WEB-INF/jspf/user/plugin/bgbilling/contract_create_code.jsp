<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="typeChangedCode">
	var form = this.form;
	
	var selectType = this;			
	var typeId = selectType.options[selectType.selectedIndex].value;

	if( typeId > 0 )
	{
		<c:forEach var="item" items="${contractTypesConfig.typeMap}">
			<c:set var="type" value="${item.value}"/>
			if( typeId == ${item.key} )
			{
				form.billingId.value = '${type.billingId}';
				form.patternId.value = '${type.patternId}';
				<c:if test="${usingCommonContract}">
					form.serviceCode.value = '${type.serviceCode}';
				</c:if>

				var html = '<option value=\'-1\'>-- без указания тарифа --</option>';
				<c:if test="${not empty type.tariffList}">
					html = '<option value=\'0\'>-- выберите тариф --</option>';
					<c:forEach var="item" items="${type.tariffList}">
						html += '<option value=\'${item.id}\'>${item.title }</option>';										
					</c:forEach>
				</c:if>	
			
				$(form).find( '#selectTariff' ).html( html );
				
				return;
			}
		</c:forEach>
	}
</c:set>	
		
<c:set var="contractCreateCode">
	var form = this.form;

	var selectType = form.selectType;			
	var typeId = selectType.options[selectType.selectedIndex].value;
	
	var selectTariff = form.selectTariff;
	var tariffId = selectTariff.options[selectTariff.selectedIndex].value;
	
	if( tariffId > 0 || tariffId == -1 )
	{
		var result = sendAJAXCommand( formUrl( form ) );
		if( result )
		{
			var contractId = result.data.contract.id;
			var contractTitle = result.data.contract.title;
			var billingId = form.billingId.value;
			
			${afterContractCreateCode}
		}
	}
	else
	{
		alert( 'Выберите тариф.' );
	}
</c:set>