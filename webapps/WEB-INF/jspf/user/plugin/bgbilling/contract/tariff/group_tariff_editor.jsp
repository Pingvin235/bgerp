<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="tariffGroup" value="${form.response.data.tariffGroup}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/contractTariff" styleId="${uiid}">
	<input type="hidden" name="action" value="updateContractTariffGroup"/>
	<html:hidden property="billingId"/> 
	<html:hidden property="contractId"/>
	<html:hidden property="id"/>
	
	<div class="in-table-cell mb1">
		<div style="width: 100%;">
			<u:sc>
				<c:set var="list" value="${form.response.data.registredTariffGroupList}"/>
				<c:set var="hiddenName" value="tariffGroupId"/>
				<c:set var="value" value="${tariffGroup.groupId}"/>
				<c:set var="style" value="width: 100%;"/>
				<c:set var="placeholder" value="Группа"/>
				<%@ include file="/WEB-INF/jspf/select_single.jsp"%>	
			</u:sc>
		</div>	
		
		<div style="white-space:nowrap;" class="pl1">
			c
			<c:set var="editable" value="true"/>
			<input type="text" name="dateFrom" value="${u:formatDate( tariffGroup.dateFrom, 'ymd' )}" id="${uiid}-dateFrom"/>	
			<c:set var="selector" value="#${uiid}-dateFrom"/>	
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			по
			<c:set var="editable" value="true"/>
			<input type="text" name="dateTo" value="${u:formatDate( tariffGroup.dateTo, 'ymd' )}" id="${uiid}-dateTo"/>	
			<c:set var="selector" value="#${uiid}-dateTo"/>	
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
		</div>
	</div>
	
	Комментарий:
	<textarea name="comment" rows="4" cols="10" style="width:100%; resize: vertical;">${tariffGroup.comment}</textarea>
	
	<%@ include file="/WEB-INF/jspf/ok_cancel_in_form.jsp"%>
</html:form>