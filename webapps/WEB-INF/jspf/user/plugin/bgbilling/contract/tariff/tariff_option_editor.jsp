<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/contractTariff"  styleId="${uiid}">
	<input type="hidden" name="method" value="activateTariffOption" />
	<html:hidden property="billingId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="returnUrl"/>

	<div class="in-table-cell">
		<ui:select-single list="${frd.availableOptionList}" hiddenName="optionId" value="${form.param.optionId}"
			onSelect="var form = $(this).closest('form')[0]; form.method.value='tariffOptionEditor'; $$.ajax.load(form, $('#${uiid}').parent())"
			style="width: 300px;" placeholder="Выберите опцию"/>

		<div class="ml1" style="display: inline-block;">
			<ui:combo-single list="${frd.activateModeList}" hiddenName="modeId" prefixText="Режим активации:" widthTextValue="200px"/>
		</div>
	</div>

	<%@ include file="/WEB-INF/jspf/ok_cancel_in_form.jsp"%>
</html:form>