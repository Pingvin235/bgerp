<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="service" value="${form.response.data.service}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/rscm" styleId="${uiid}">
	<input type="hidden" name="action" value="serviceUpdate" />
	<html:hidden property="contractId" value="${form.param.contractId}" />
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>

	<div class="in-table-cell mb1">
		<div style="width: 50%;">
			<ui:select-single hiddenName="serviceId" value="${service.serviceId}"
				placeholder="Услуга" style="width: 100%;" list="${form.response.data.serviceTypeList}">
				<jsp:attribute name="inputAttrs">
					<c:if test="${service.id gt 0}">disabled="disabled"</c:if>
				</jsp:attribute>
			</ui:select-single>
		</div>

		<div style="white-space:nowrap;" class="pl1" style="width: 50%;">
			Дата:
			<ui:date-time paramName="date" value="${tu.format( service.date, 'ymd' )}"/>
			Кол.-во:
			<input type="text" value="${service.amount}" name="amount" style="text-align: center;"/>
		</div>
	</div>

	Комментарий:
	<textarea name="comment" rows="4" cols="10" style="width:100%; resize: vertical;">${service.comment}</textarea>

	<div class="mt1">
		<button type="button" class="btn-grey" onclick="if(sendAJAXCommand(formUrl(this.form))) { $$.ajax.load('${form.returnUrl}', $('#${uiid}').parent()); }">OK</button>
		<button type="button" class="btn-grey ml1" onclick="$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent());">Oтмена</button>
	</div>
</html:form>