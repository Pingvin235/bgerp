<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
	
<div class="in-table-cell">
	<form action="/user/plugin/bgbilling/proto/contract.do">
		<input type="hidden" name="action" value="updateMode"/>
		<input type="hidden" name="contractId" value="${form.param.contractId}"/>
		<input type="hidden" name="billingId" value="${form.param.billingId}"/>
		
		<u:sc>
			<c:set var="valuesHtml">
				<li value="0">Кредит</li>
				<li value="1">Дебет</li>
			</c:set>
			<c:set var="hiddenName" value="value"/> 
			<c:set var="value" value="${form.response.data.contractInfo.mode}"/>
			<c:set var="prefixText" value="Режим (изменить):"/>
			<c:set var="widthTextValue" value="100px"/>
			<c:set var="onSelect" value="if( sendAJAXCommand( formUrl( $hidden.closest('form') ) ) ){ openUrlToParent( '${form.requestUrl}', $('#${uiid}') ); }"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
		</u:sc>
	</form>
	
	<html:form action="/user/plugin/bgbilling/proto/contract" style="width: 100%;">
		<input type="hidden" name="action" value="modeLog"/>
		<html:hidden property="contractId"/>
		<html:hidden property="billingId"/>
	
		<c:set var="nextCommand" value="; openUrlToParent( formUrl( this.form ), $('#${uiid}') );"/>
		<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
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
			<td nowrap="nowrap">${u:formatDate( item.time, 'ymdhms' )}</td>
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