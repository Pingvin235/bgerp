<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<%@ taglib tagdir="/WEB-INF/tags/plugin/report" prefix="report"%>

<shell:title text="${l.l('Report')}"/>
<shell:state text="${l.l('Invoice Register')}"/>

<div class="report">
	<html:form action="${form.httpRequestURI}">
		<ui:date-month value="${form.param.dateFrom}"/>

		<c:set var="outId" value="${u:uiid()}"/>
		<ui:button type="out" id="${outId}" onclick="$$.ajax.loadContent(this)" styleClass="ml1 more out"/>
	</html:form>

	<c:set var="list" value="${form.response.data.list}"/>

	<c:if test="${not empty list}">
		<div class="data mt1 w100p" style="overflow: auto;">
			<%-- for payment date dialog --%>
			<c:set var="hiddenUiid" value="${u:uiid()}"/>
			<input type='hidden' id="${hiddenUiid}"/>

			<table class="data hl">
				<report:headers data="${data}"/>
				<c:forEach var="r" items="${list}">
					<tr>
						<td><ui:process-link id="${r.get('process_id')}"/></td>
						<td>${r.get('invoice_type')}</td>
						<td>${r.get('invoice_amount')}</td>
						<td>${r.getString('invoice_created_date')}</td>
						<td>${r.getString('invoice_number')}</td>
						<td>
							<c:set var="date" value="${r.getString('invoice_payment_date')}"/>
							<c:set var="invoiceId" value="${r.get('invoice_id')}"/>

							<c:choose>
								<c:when test="${empty date}">
									<c:url var="url" value="/user/plugin/invoice/invoice.do">
										<c:param name="action" value="paid"/>
										<c:param name="id" value="${invoiceId}"/>
									</c:url>
									<a href="#" title="${l.l('Paid')}" onclick="$$.invoice.paid('${hiddenUiid}', '${url}').done(() => $('#${outId}').click()); return false;">${l.l('paid')}</a>
								</c:when>
								<c:otherwise>
									<c:url var="url" value="/user/plugin/invoice/invoice.do">
										<c:param name="action" value="unpaid"/>
										<c:param name="id" value="${invoiceId}"/>
									</c:url>
									<a href="#" title="${l.l('Unpaid')}" onclick="$$.ajax.post('${url}').done(() => $('#${outId}').click()); return false;">${date}</a>
								</c:otherwise>
							</c:choose>
						</td>
						<td>${r.get('customer_title')}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>
</div>