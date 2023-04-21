<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="in-table-cell">
	<form action="/user/plugin/bgbilling/proto/contract.do">
		<input type="hidden" name="action" value="updateFace"/>
		<input type="hidden" name="contractId" value="${form.param.contractId}"/>
		<input type="hidden" name="billingId" value="${form.param.billingId}"/>

		<u:sc>
			<c:set var="valuesHtml">
				<li value="0">Физическое</li>
				<li value="1">Юридическое</li>
			</c:set>
			<c:set var="hiddenName" value="value"/>
			<c:set var="value" value="${form.response.data.contractInfo.face}"/>
			<c:set var="prefixText" value="Лицо (изменить):"/>
			<c:set var="widthTextValue" value="100px"/>
			<c:set var="onSelect" value="if( sendAJAXCommand( formUrl( $hidden.closest('form') ) ) ){ $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()); }"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
		</u:sc>
	</form>

	<html:form action="/user/plugin/bgbilling/proto/contract" style="width: 100%;">
		<input type="hidden" name="action" value="faceLog"/>
		<html:hidden property="contractId"/>
		<html:hidden property="billingId"/>

		<ui:page-control nextCommand="; $$.ajax.load(this.form, $('#${uiid}').parent()); />
	</html:form>
</div>

<table class="data mt1" style="width: 100%;" id="${uiid}">
	<tr>
		<td>Дата</td>
		<td>Пользователь</td>
		<td width="100%">Лицо</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td nowrap="nowrap">${tu.format( item.time, 'ymdhms' )}</td>
			<td nowrap="nowrap">${item.user}</td>
			<td>${item.face}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function()
	{
		$("#${contractTreeId} #face").text( ${form.response.data.contractInfo.face} == 0 ? "Физическое" : "Юридическое" );
	})
</script>