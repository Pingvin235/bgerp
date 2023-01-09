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
			<c:if test="${service.id gt 0}">
				<c:set var="inputAttrs">disabled="disabled"</c:set>
			</c:if>
			<ui:select-single list="${form.response.data.serviceTypeList}" hiddenName="serviceId" value="${service.serviceId}"
				inputAttrs="${inputAttrs}" style="width: 100%;" placeholder="Услуга"/>
		</div>
		<div style="white-space:nowrap;" class="pl1">
			c
			<c:set var="editable" value="true"/>
			<input type="text" name="dateFrom" value="${tu.format( service.dateFrom, 'ymd' )}" id="${uiid}-dateFrom"/>
			<c:set var="selector" value="#${uiid}-dateFrom"/>
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
			по
			<c:set var="editable" value="true"/>
			<input type="text" name="dateTo" value="${tu.format( service.dateTo, 'ymd' )}" id="${uiid}-dateTo"/>
			<c:set var="selector" value="#${uiid}-dateTo"/>
			<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
		</div>

		<div style="width: 50%;" class="pl1">
			<ui:select-single list="${form.response.data.objectList}" hiddenName="objectId" value="${service.objectId}" style="width: 100%;" placeholder="Объект (необязательно)"/>
		</div>
	</div>

	Комментарий:
	<textarea name="comment" rows="4" cols="10" style="width:100%; resize: vertical;">${service.comment}</textarea>

	<div class="mt1">
		<button type="button" class="btn-grey" onclick="if(sendAJAXCommand(formUrl(this.form))) { openUrlToParent('${form.returnUrl}',$('#${uiid}')); }">OK</button>
		<button type="button" class="btn-grey ml1" onclick="openUrlToParent('${form.returnUrl}',$('#${uiid}'));">Oтмена</button>
	</div>
</html:form>