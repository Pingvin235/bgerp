<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="service" value="${frd.service}"/>

<h1>Редактор</h1>

<html:form action="${form.httpRequestURI}">
	<input type="hidden" name="action" value="serviceUpdate" />
	<html:hidden property="contractId" value="${form.param.contractId}" />
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>

	<div class="in-table-cell mb1">
		<div style="width: 50%;">
			<ui:select-single hiddenName="serviceId" value="${service.serviceId}"
				placeholder="Услуга" style="width: 100%;" list="${frd.serviceTypeList}">
				<jsp:attribute name="inputAttrs">
					<c:if test="${service.id gt 0}">disabled="disabled"</c:if>
				</jsp:attribute>
			</ui:select-single>
		</div>

		<div style="white-space:nowrap;" class="pl1" style="width: 50%;">
			Дата:
			<ui:date-time paramName="date" value="${tu.format(service.date, 'ymd')}"/>
			Кол.-во:
			<input type="text" value="${service.amount}" name="amount" style="text-align: center;"/>
		</div>
	</div>

	Комментарий:
	<textarea name="comment" rows="4" cols="10" style="width:100%; resize: vertical;">${service.comment}</textarea>

	<div class="mt1">
		<ui:button type="ok" onclick="$$.ajax.post(this.form).done(() => $$.ajax.load('${form.returnUrl}', $(this.form).parent()))"/>
		<ui:button type="cancel" styleClass="ml1" onclick="$$.ajax.load('${form.returnUrl}', $(this.form).parent());"/>
	</div>
</html:form>