<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="commonContrat" value="${u:uiid()}"/>
<c:set var="selectType" value="${u:uiid()}"/>

<c:set var="usingCommonContract" value="${not empty setup['bgbilling:commonContract.addressParamId'] }"/>

<c:set var="createButtonsUiid" value="${u:uiid()}"/>
<c:set var="createCommonContractUiid" value="${u:uiid()}"/>
<c:set var="createContractUiid" value="${u:uiid()}"/>

<c:set var="tabsUiid" value="bgbilling-customerContractList-${customer.id}"/>

<c:set var="reopenCommand">openUrlToParent( '${form.requestUrl}', $('#${createButtonsUiid}') )</c:set>

<div id="${createButtonsUiid}">
	<c:if test="${usingCommonContract}">
		<button type="button" class="btn-green mr1" onclick="$('#${createButtonsUiid}').hide(); $('#${createCommonContractUiid}').show();">Создать единый договор</button>
	</c:if>
	<button type="button" class="btn-green" onclick="$('#${createButtonsUiid}').hide(); $('#${createContractUiid}').show();">Создать договор</button>
</div>

<c:if test="${usingCommonContract}">
	<html:form action="/user/plugin/bgbilling/commonContract" styleClass="in-table-cell nowrap mb1" styleId="${createCommonContractUiid}" style="display: none;">
		<input type="hidden" name="action" value="commonContractCreate"/>
		<html:hidden property="customerId"/>
		
		<div style="width:100%;" class="pr1">
			<u:sc>
				<c:set var="valuesHtml">
					<c:forEach var="item" items="${customerAddressMap}">
						<li value="${item.key}">${item.value.value}</li>
					</c:forEach>
				</c:set>
				<c:set var="hiddenName" value="addressParamPos"/>
				<c:set var="prefixText" value="Адрес:"/>
				<c:set var="style" value="width: 100%;"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
			</u:sc>			
		</div>
		<div>
			<button type="button" class="btn-grey" 
				onclick="var result; if( confirm( 'Создать новый единый договор с выбранным адресом?' ) && (result = sendAJAXCommand( formUrl( this.form ) ) ) ){ ${reopenCommand}; $('#${tabsUiid}').tabs( 'showTab', 'commonContract-' + result.data.contract.id ) }">OK</button>
			<button type="button" class="btn-grey ml05" onclick="$('#${createCommonContractUiid}').hide(); $('#${createButtonsUiid}').show();">${l.l('Отмена')}</button>	
		</div>	
	</html:form> 
</c:if>

<c:set var="commonContractChangedScript">
	var typeMap = new Array();
	var typeObject;
	
	<c:forEach var="item" items="${contractTypesConfig.typeMap}">	
		typeObject = new Object();
		typeObject.value = '${item.key}';
		typeObject.areaId = '${item.value.commonContractAreaCode}';
		typeObject.title = '${item.value.title}';
		
		<c:choose>
			<c:when test="${item.value.commonContractAreaCode == 0}">
				typeObject.hidden = false;
			</c:when>
			<c:otherwise>
				typeObject.hidden = true;
			</c:otherwise>
		</c:choose>
		
		typeMap.push(typeObject);
	</c:forEach>

	var areaId = $(item).attr( 'areaid' );
		
	var $ul = $('#${createContractUiid} #selectType ul.drop');
	$ul.html("");
	
	typeMap.forEach( function( element, index )
	{
		if( element.areaId == areaId )
		{
			$ul.append( $( '<li></li>' ).attr( 'value', element.value ).attr( 'areaid', element.areaId ).text( element.title ) ); 
		}
	});

	uiComboSingleInit( $('#${createContractUiid} #selectType div.combo' ) );
</c:set>

<html:form action="/user/plugin/bgbilling/contract" style="display: none;" styleClass="in-table-cell nowrap in-pr05" styleId="${createContractUiid}">
	<input type="hidden" name="action" value="contractCreate"/>
	<input type="hidden" name="date" value="${currentDate}"/>
	<html:hidden property="customerId"/>
	<input type="hidden" name="billingId"/>
	<input type="hidden" name="patternId"/>	
	<input type="hidden" name="comment" value="${fn:escapeXml(customer.title)}"/>
		
	<c:if test="${usingCommonContract}">
		<div>
			<input type="hidden" name="serviceCode"/>
			<u:sc>
				<c:set var="valuesHtml">
					<li value="0" areaId="0">--- без единого договора ---</option>
					<c:forEach var="item" items="${commonContractList}">
						<li value="${item.id}" areaId="${item.areaId}">${item.formatedNumber}</option>
					</c:forEach>
				</c:set>
				<c:set var="hiddenName" value="commonContractId"/>
				<c:set var="prefixText" value="Единый договор:"/>
				<c:set var="style" value="width: 100%;"/>
				<c:set var="onSelect" value="${commonContractChangedScript}"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
			</u:sc>			
		</div>
	</c:if>
			
	<div>
		Номер:
		<input type="text" name="title"/>
	</div>
	
	<%@ include file="contract_create_code_new_iface.jsp"%>
			
	<div style="width: 50%;" id="selectType">
		<u:sc>
			<c:set var="valuesHtml">
				<c:forEach var="item" items="${contractTypesConfig.typeMap}">
					<c:choose>
						<c:when test="${item.value.commonContractAreaCode == 0}">
							<li value="${item.key}" areaId="${item.value.commonContractAreaCode}">${item.value.title}</li>
						</c:when>
						<c:otherwise>
							<li value="${item.key}" hidden="hidden" areaId="${item.value.commonContractAreaCode}">${item.value.title}</li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</c:set>
			<c:set var="hiddenName" value="typeId"/>
			<c:set var="prefixText" value="Тип договора:"/>
			<c:set var="style" value="width: 100%;"/>
			<c:set var="onSelect" value="${typeChangedCode}"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
		</u:sc>
	</div>
	<div style="width: 50%;" id="selectTariff">
		<%-- сюда динамически загружаются тарифы --%>
		<u:sc>
			<c:set var="hiddenName" value="tariffId"/>
			<c:set var="prefixText" value="Тариф:"/>
			<c:set var="style" value="width: 100%;"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
		</u:sc>
	
		<script>
			$(function() 
			{
				${typeChangedCode}
			})		
		</script>
	</div>
	<div>
		<button type="button" class="btn-grey ml1" onclick="${contractCreateCode};">OK</button>
		<button type="button" class="btn-grey ml05" onclick="$('#${createContractUiid}').hide(); $('#${createButtonsUiid}').show();">${l.l('Отмена')}</button>
	</div>
</html:form>

<div id="${tabsUiid}" class="mt1">
	<ul></ul>
</div>

<script>
	$(function() 
	{
		var $tabs = $( "#${tabsUiid}" ).tabs( { refreshButton: true });
		
		<%-- в первую очередь - единые договора --%>
		<c:if test="${usingCommonContract}">
			<c:forEach items="${commonContractList}" var="commonContract" varStatus="status">
				<c:set var="liAttrs"> id='commonContract-${commonContract.id}'</c:set>
				<c:if test="${status.last}">
					<c:set var="liAttrs">${liAttrs} style='margin-right: 1em;'</c:set>
				</c:if>
				$tabs.tabs( 'add', '/user/plugin/bgbilling/commonContract.do?&id=${commonContract.id}', '${commonContract.formatedNumber}', "${liAttrs}" );
			</c:forEach>
		</c:if>
		
		<%-- далее - договора, не привязанные к единым договорам --%>
		<c:forEach items="${form.response.data.list}" var="link">
			<c:set var="billingId" value="${fn:substringAfter( link.linkedObjectType, ':' )}"/>
			<c:set var="customerId" value="${form.response.data.customerId}"/>
		
			<c:remove var="linkedToCommonContract"/>
			<c:forEach items="${commonContractList}" var="commonContract">
				<c:if test="${empty linkedToCommonContract and fn:startsWith( link.linkedObjectTitle, commonContract.formatedNumber )}">
					<c:set var="linkedToCommonContract" value="true"/>
				</c:if>
			</c:forEach>
			
			<c:if test="${empty linkedToCommonContract}">
				<c:set var="liAttrs"> id='${billingId}-${link.linkedObjectId}'</c:set>			
				$tabs.tabs( 'add', '/user/plugin/bgbilling/contract.do?billingId=${billingId}&id=${link.linkedObjectId}&inBuffer=0', '${link.linkedObjectTitle}', "${liAttrs}" );
			</c:if>
		</c:forEach>

		$tabs.trigger("tabsinit");
	})	
</script>