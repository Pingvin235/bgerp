<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="commonContrat" value="${u:uiid()}"/>
<c:set var="selectType" value="${u:uiid()}"/>

<c:set var="createButtonsUiid" value="${u:uiid()}"/>
<c:set var="createCommonContractUiid" value="${u:uiid()}"/>
<c:set var="createContractUiid" value="${u:uiid()}"/>

<c:set var="tabsUiid" value="bgbilling-customerContractList-${customer.id}"/>

<c:set var="reopenCommand">openUrlToParent( '${form.requestUrl}', $('#${createButtonsUiid}') )</c:set>

<div id="${createButtonsUiid}">
	<button type="button" class="btn-green" onclick="$('#${createButtonsUiid}').hide(); $('#${createContractUiid}').show();">Создать договор</button>
</div>

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

	$$.ui.comboSingleInit( $('#${createContractUiid} #selectType div.combo' ) );
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

		<c:forEach items="${form.response.data.list}" var="link">
			<c:set var="billingId" value="${su.substringAfter( link.linkedObjectType, ':' )}"/>
			<c:set var="customerId" value="${form.response.data.customerId}"/>

			<c:set var="liAttrs"> id='${billingId}-${link.linkedObjectId}'</c:set>
			$tabs.tabs( 'add', '/user/plugin/bgbilling/contract.do?billingId=${billingId}&id=${link.linkedObjectId}&inBuffer=0', '${link.linkedObjectTitle}', "${liAttrs}" );
		</c:forEach>

		$tabs.trigger("tabsinit");
	})
</script>