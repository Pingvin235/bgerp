<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="in-table-cell">
	<form action="/user/plugin/bgbilling/proto/contract.do">
		<input type="hidden" name="action" value="updateMode"/>
		<input type="hidden" name="contractId" value="${form.param.contractId}"/>
		<input type="hidden" name="billingId" value="${form.param.billingId}"/>

		<ui:combo-single hiddenName="value" value="${form.response.data.contractInfo.mode}" prefixText="Режим (изменить):" widthTextValue="100px"
			onSelect="if( sendAJAXCommand( formUrl( $hidden.closest('form') ) ) ){ $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()); }">
			<jsp:attribute name="valuesHtml">
				<li value="0">Кредит</li>
				<li value="1">Дебет</li>
			</jsp:attribute>
		</ui:combo-single>
	</form>

	<html:form action="/user/plugin/bgbilling/proto/contract" style="width: 100%;">
		<input type="hidden" name="action" value="modeLog"/>
		<html:hidden property="contractId"/>
		<html:hidden property="billingId"/>

		<ui:page-control nextCommand="; $$.ajax.load(this.form, $('#${uiid}').parent()); />
	</html:form>
</div>

<table class="data mt1" style="width: 100%;" id="${uiid}">
	<tr>
		<td>Дата</td>
		<td>Пользователь</td>
		<td width="100%">Режим</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td nowrap="nowrap">${tu.format( item.time, 'ymdhms' )}</td>
			<td nowrap="nowrap">${item.user}</td>
			<td>${item.mode}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function()
	{
		$("#${contractTreeId} #mode").text( ${form.response.data.contractInfo.mode} == 0 ? "Кредит" : "Дебет" );
	})
</script>