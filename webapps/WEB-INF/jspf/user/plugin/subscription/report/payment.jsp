<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<%@ taglib tagdir="/WEB-INF/tags/plugin/report" prefix="report"%>

<shell:title ltext="Report"/>
<shell:state ltext="Subscription Payments"/>

<div class="report center1020">
	<html:form action="${form.httpRequestURI}">
		<ui:date-month/>

		<c:set var="config" value="${ctxSetup.getConfig('org.bgerp.plugin.bil.subscription.Config')}"/>
		<ui:combo-single hiddenName="subscriptionId" list="${config.subscriptions}" prefixText="${l.l('Subscription:')}"/>

		<ui:button type="out" styleClass="ml1 mr1 more out" onclick="$$.ajax.loadContent(this)"/>

		<%-- <report:more data="${data}"/> --%>

		<ui:page-control nextCommand="; $$.ajax.loadContent(this)" styleClass="more"/>
	</html:form>

	<c:if test="${not empty form.param.dateFrom}">
		<div class="data mt1 w100p" style="overflow: auto;">
			<h2>${l.l('Info')}</h2>

			<%-- TODO: Change to table without border.  --%>
			<div>${l.l('User')}: <b>${ctxUser.title}</b></div>
			<c:if test="${not empty form.response.data.incomingTaxPercent}">
				<div class="mt05">${l.l('Tax')}: <b>${form.response.data.incomingTaxPercent}</b>%</div>
			</c:if>

			<h2>${l.l('Details')}</h2>

			<table class="data hl">
				<report:headers data="${data}"/>
				<c:forEach var="r" items="${form.response.data.list}">
					<tr>
						<td><ui:process-link id="${r.get('subscription_id')}"/></td>
						<td>${r.get('customer_title')}</td>
						<td>${r.get('payment_amount')}</td>
						<td>${r.get('discount')}</td>
						<td>${r.get('service_cost')}</td>
						<td>${r.get('owners_amount')}</td>
						<td>${r.get('product_description')}</td>
						<td>${r.get('product_owner')}</td>
						<td>${r.get('product_cost')}</td>
					</tr>
				</c:forEach>
			</table>

			<h2>${l.l('Total')}</h2>

			<table class="data hl">
				<tr>
					<td>${l.l('Whom')}</td>
					<td>${l.l('Amount')}</td>
				</tr>
				<c:forEach items="${form.response.data.ownerAmount}" var="entry">
					<tr>
						<td><ui:user-link id="${u:int(entry.key)}"/></td>
						<td>${entry.value}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>
</div>