<%@ tag body-content="empty" pageEncoding="UTF-8" description="Input day's range in month"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="hiddenName" description="hidden input's name which contains the start of the period (default: dateFrom)"%>
<%@ attribute name="value" description="current value in dd.MM.yyyy format of the first day for a month"%>

<c:if test="${empty hiddenName}">
	<c:set var="hiddenName" value="dateFrom"/>
</c:if>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="date-month" id="${uiid}">
	<input type="hidden" id="dateFrom" name="${hiddenName}" value="${value}"/>

	<button class="btn-white icon" id="prev" onclick="return false"><i class="ti-angle-left"></i></button>
	<button class="btn-white" id="month" onclick="return false">&nbsp;</button>
	<button class="btn-white icon" id="next" onclick="return false"><i class="ti-angle-right"></i></button>

	<script>
		$(function () {
			$$.ui.monthSelectInit($('#${uiid}'));
		})
	</script>
</div>