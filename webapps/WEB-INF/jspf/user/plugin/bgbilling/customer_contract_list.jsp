<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="createButtonsUiid" value="${u:uiid()}"/>
<c:set var="createContractUiid" value="${u:uiid()}"/>

<c:set var="tabsUiid" value="bgbilling-customerContractList-${customer.id}"/>

<div id="${createButtonsUiid}">
	<button type="button" class="btn-green" onclick="$('#${createButtonsUiid}').hide(); $('#${createContractUiid}').show();">Создать договор</button>
</div>

<html:form action="${form.httpRequestURI}" style="display: none;" styleClass="in-table-cell nowrap in-pr05" styleId="${createContractUiid}">
	<input type="hidden" name="action" value="contractCreate"/>
	<input type="hidden" name="date" value="${currentDate}"/>
	<html:hidden property="customerId"/>
	<input type="hidden" name="comment" value="${u.escapeXml(customer.title)}"/>

	<div>
		Номер:
		<input type="text" name="title"/>
	</div>

	<c:set var="typeChangedCode" value="$$.bgbilling.contract.createTariff('${createContractUiid}')"/>

	<div style="width: 50%;">
		<ui:combo-single hiddenName="typeId" onSelect="${typeChangedCode}" prefixText="Тип договора:" style="width: 100%;">
			<jsp:attribute name="valuesHtml">
				<c:forEach var="item" items="${contractTypesConfig.typeMap}">
					<li value="${item.key}">${item.value.title}</li>
				</c:forEach>
			</jsp:attribute>
		</ui:combo-single>
	</div>
	<div style="width: 50%;" id="selectTariff">
		<%-- сюда динамически загружаются тарифы --%>
	</div>
	<div>
		<button type="button" class="btn-grey ml1" onclick="
			$$.bgbilling.contract
				.create(this)
				.done(() => $$.ajax.load('${form.requestUrl}', $('#${createContractUiid}').parent()))
		">OK</button>
		<button type="button" class="btn-white ml05" onclick="$('#${createContractUiid}').hide(); $('#${createButtonsUiid}').show();">Отмена</button>
	</div>
</html:form>

<div id="${tabsUiid}" class="mt1">
	<ul></ul>
</div>

<script>
	$(function () {
		${typeChangedCode}

		const $tabs = $( "#${tabsUiid}" ).tabs({ refreshButton: true });

		<c:forEach items="${form.response.data.list}" var="link">
			<c:set var="billingId" value="${su.substringAfter( link.linkedObjectType, ':' )}"/>
			<c:set var="customerId" value="${form.response.data.customerId}"/>

			<c:set var="liAttrs"> id='${billingId}-${link.linkedObjectId}'</c:set>
			$tabs.tabs('add', '/user/plugin/bgbilling/contract.do?billingId=${billingId}&id=${link.linkedObjectId}&inBuffer=0', '${link.linkedObjectTitle}', "${liAttrs}");
		</c:forEach>

		$tabs.trigger("tabsinit");
	})
</script>