<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="service" value="${frd.service}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/npay" styleId="${uiid}">
	<input type="hidden" name="method" value="serviceUpdate" />
	<html:hidden property="contractId" value="${form.param.contractId}" />
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>

	<div class="in-table-cell mb1">
		<div style="width: 50%;">
			<c:if test="${service.id gt 0}">
				<c:set var="inputAttrs">disabled="disabled"</c:set>
			</c:if>
			<ui:select-single list="${frd.serviceTypeList}" name="serviceId" value="${service.serviceId}" inputAttrs="${inputAttrs}" style="width: 100%;" placeholder="Услуга"/>
		</div>
		<div style="white-space:nowrap;" class="pl1">
			c
			<ui:date-time name="dateFrom" value="${tu.format(service.dateFrom, 'ymd')}"/>
			по
			<ui:date-time name="dateTo" value="${tu.format(service.dateTo, 'ymd')}"/>
		</div>

		<div style="width: 50%;" class="pl1">
			<ui:select-single list="${frd.objectList}" name="objectId" value="${service.objectId}" style="width: 100%;" placeholder="Объект (необязательно)"/>
		</div>
	</div>

	Комментарий:
	<textarea name="comment" rows="4" cols="10" style="width:100%; resize: vertical;">${service.comment}</textarea>

	<div class="mt1">
		<c:set var="returnCommand" value="$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent())"/>
		<button type="button" class="btn-grey" onclick="$$.ajax.post(this).done(() => ${returnCommand})">OK</button>
		<button type="button" class="btn-white ml1" onclick="${returnCommand}">Oтмена</button>
	</div>
</html:form>