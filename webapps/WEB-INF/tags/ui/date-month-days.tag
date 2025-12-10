<%@ tag body-content="empty" pageEncoding="UTF-8" description="Input day's range in month"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="nameFrom" description="hidden input's name which contains the start of the period (default: dateFrom)"%>
<%@ attribute name="nameTo" description="hidden input's name which contains the end of the period (default: dateTo)"%>

<c:if test="${empty nameFrom}">
	<c:set var="nameFrom" value="dateFrom"/>
</c:if>
<c:if test="${empty nameTo}">
	<c:set var="nameTo" value="dateTo"/>
</c:if>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="date-month-days" id="${uiid}">
	<input type="hidden" id="dateFrom" name="${nameFrom}" value="${form.param[nameFrom]}"/>
	<input type="hidden" id="dateTo" name="${nameTo}" value="${form.param[nameTo]}"/>

	<button class="btn-white icon" id="prev" onclick="return false"><i class="ti-angle-left"></i></button>
	<button class="btn-white" id="month" onclick="return false"></button>
	<button class="btn-white icon" id="next" onclick="return false"><i class="ti-angle-right"></i></button>
	<input type="text" id="dayFrom" maxlength="2" name="${nameFrom}Day" value="${form.param[nameFrom.concat('Day')]}" placeholder="${l.l('с')}" title="${l.l('Day From')}"/>
	<input type="text" id="dayTo" maxlength="2" name="${nameTo}Day" value="${form.param[nameTo.concat('Day')]}" placeholder="${l.l('по')}" title="${l.l('Day To')}"/>
</div>

<script>
	$(function () {
		$$.ui.monthDaysSelectInit($('#${uiid}'));
	})
</script>