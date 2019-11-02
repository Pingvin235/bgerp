<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="service" value="${form.response.data.service}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/npay" styleId="${uiid}">
	<input type="hidden" name="action" value="serviceUpdate" />
	<html:hidden property="contractId" value="${form.param.contractId}" />
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>
	
	<div class="in-table-cell mb1">
		<div style="width: 50%;">
			<u:sc>
				<c:set var="list" value="${form.response.data.serviceTypeList}"/>
				<c:set var="hiddenName" value="serviceId"/>
				<c:set var="value" value="${service.serviceId}"/>
				<c:set var="style" value="width: 100%;"/>
				<c:set var="placeholder" value="Услуга"/>
				<c:if test="${service.id gt 0}">
					<c:set var="inputAttrs">disabled="disabled"</c:set>
				</c:if>
				<%@ include file="/WEB-INF/jspf/select_single.jsp"%>	
			</u:sc>
		</div>	
		
		<div style="white-space:nowrap;" class="pl1">
			c
			<c:set var="editable" value="true"/>
			<input type="text" name="dateFrom" value="${u:formatDate( service.dateFrom, 'ymd' )}" id="${uiid}-dateFrom"/>	
			<c:set var="selector" value="#${uiid}-dateFrom"/>	
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			по
			<c:set var="editable" value="true"/>
			<input type="text" name="dateTo" value="${u:formatDate( service.dateTo, 'ymd' )}" id="${uiid}-dateTo"/>	
			<c:set var="selector" value="#${uiid}-dateTo"/>	
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
		</div>
		
		<div style="width: 50%;" class="pl1">
			<u:sc>
				<c:set var="list" value="${form.response.data.objectList}"/>
				<c:set var="hiddenName" value="objectId"/>
				<c:set var="value" value="${service.objectId}"/>
				<c:set var="style" value="width: 100%;"/>
				<c:set var="placeholder" value="Объект (необязательно)"/>
				<%@ include file="/WEB-INF/jspf/select_single.jsp"%>	
			</u:sc>
		</div>	
	</div>
	
	Комментарий:
	<textarea name="comment" rows="4" cols="10" style="width:100%; resize: vertical;">${service.comment}</textarea>
	
	<div class="mt1">
		<button type="button" class="btn-grey" onclick="if(sendAJAXCommand(formUrl(this.form))) { openUrlToParent('${form.returnUrl}',$('#${uiid}')); }">OK</button>
		<button type="button" class="btn-grey ml1" onclick="openUrlToParent('${form.returnUrl}',$('#${uiid}'));">Oтмена</button>
	</div>
</html:form>