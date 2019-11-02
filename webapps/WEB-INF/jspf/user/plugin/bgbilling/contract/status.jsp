<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/bgbilling/proto/contract" styleId="${uiid}">
	<html:hidden property="billingId"/> 
	<html:hidden property="contractId"/>
	<input type="hidden" name="action" value="updateStatus"/>
	
	<table style="width: 100%;"> 
		<tr class="in-pr05">
			<td>С даты:</td>
			<td nowrap="nowrap">	
				<c:set var="editable" value="true"/>
				<input type="text" name="dateFrom" value="${script.dateFrom}" id="${uiid}-dateFrom"/>	
				<c:set var="selector" value="#${uiid}-dateFrom"/>	
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
				по дату:
				<c:set var="editable" value="true"/>
				<input type="text" name="dateTo" value="${script.dateTo}" id="${uiid}-dateTo"/>	
				<c:set var="selector" value="#${uiid}-dateTo"/>	
				<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			<td>
			<td width="100%" class="pl05">
				<u:sc>
					<c:set var="list" value="${form.response.data.availableStatusList}"/>
					<c:set var="hiddenName" value="statusId"/>
					<c:set var="style" value="width: 100%;"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
				</u:sc>
			</td>
			<td></td>
		</tr>
		
		<c:set var="saveCommand">if( sendAJAXCommand( formUrl( $(this).closest( 'form' ) ) ) ){ openUrlToParent( '${form.requestUrl}', $('#${uiid}') ) }</c:set>
		<tr class="in-pt05 in-pr05">
			<td>Комментарий:</td>
			<td colspan="3">
				<input name="comment" onkeypress="if( enterPressed( event ) ){ ${saveCommand}; return false; }" style="width: 100%"/>
			</td>
			<td>
				<button type="button" type="button" class="btn-grey ml05" onclick="${saveCommand}">Изменить</button>
			</td>
		</tr>
	</table>
</html:form>

<table class="data mt1" style="width:100%;">
	<tr class="header">
		<td>Период</td>
		<td>Статус</td>			
		<td width="100%">Комментарий</td>
	</tr>
	
	<c:forEach var="status" items="${form.response.data.statusList}">
		<tr>
			<td nowrap="nowrap">${u:formatPeriod( status.dateFrom, status.dateTo, 'ymd' )}</td>
			<td nowrap="nowrap" >${status.status}</td>				
			<td>${status.comment}</td>
		</tr>
	</c:forEach>
</table>

<h2>История изменения статуса</h2>

<table class="data" style="width:100%;">
	<tr class="header">
		<td>Период</td>
		<td>Статус</td>
		<td>Время</td>
		<td>Пользователь</td>			
		<td width="100%">Комментарий</td>
	</tr>
	
	<c:forEach var="item" items="${form.response.data.statusLog}">
		<tr>
			<td nowrap="nowrap">${u:formatPeriod( item.dateFrom, item.dateTo, 'ymd' )}</td>
			<td nowrap="nowrap">${item.status}</td>
			<td nowrap="nowrap">${u:formatDate( item.time, 'ymdhms' )}</td>
			<td nowrap="nowrap">${item.user}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function()
	{
		$('#${contractTreeId} #treeTable td#status').text( '${contractInfo.status}' );	
	})
</script>