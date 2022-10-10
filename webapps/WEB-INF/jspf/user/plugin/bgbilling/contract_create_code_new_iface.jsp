<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="typeChangedCode">
	var form = $("#${createContractUiid}")[0];
	var typeId = form.typeId.value;
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

				var html = '<li value=\'-1\'>-- без указания тарифа --</li>';
				<c:if test="${not empty type.tariffList}">
					<c:forEach var="item" items="${type.tariffList}">
						html += '<li value=\'${item.id}\'>${item.title }</li>';
					</c:forEach>
				</c:if>

				$(form).find( '#selectTariff ul.drop' ).html( html );
				$$.ui.comboSingleInit( $(form).find( '#selectTariff div.combo' ) );

				return;
			}
		</c:forEach>
	}
</c:set>

<c:set var="contractCreateCode">
	var form = this.form;

	var typeId = form.typeId.value;
	var tariffId = form.tariffId.value;

	if( tariffId > 0 || tariffId == -1 )
	{
		var result = sendAJAXCommand( formUrl( form ) );
		if( result )
		{
			var contractId = result.data.contract.id;
			var contractTitle = result.data.contract.title;
			var billingId = form.billingId.value;

			${afterContractCreateCode}

			bgbilling_openContract( billingId, contractId );
		}
	}
	else
	{
		alert( 'Выберите тариф.' );
	}
</c:set>