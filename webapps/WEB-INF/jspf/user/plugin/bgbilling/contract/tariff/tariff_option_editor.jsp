<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/contractTariff"  styleId="${uiid}">
	<input type="hidden" name="action" value="activateTariffOption" />
	<html:hidden property="billingId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="returnUrl"/>

	<div class="in-table-cell">
		<ui:select-single list="${form.response.data.availableOptionList}" hiddenName="optionId" value="${form.param.optionId}"
			onSelect="var form = $(this).closest('form')[0]; form.action.value='tariffOptionEditor'; openUrlToParent( formUrl( form ), $('#${uiid}') )"
			style="width: 300px;" placeholder="Выберите опцию"/>

		<div class="ml1" style="display: inline-block;">
			<u:sc>
				<c:set var="list" value="${form.response.data.activateModeList}"/>
				<c:set var="hiddenName" value="modeId"/>
				<c:set var="prefixText" value="Режим активации:"/>
				<c:set var="widthTextValue" value="200px"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>
		</div>
	</div>

	<%@ include file="/WEB-INF/jspf/ok_cancel_in_form.jsp"%>
</html:form>