<%@ tag body-content="empty" pageEncoding="UTF-8" description="Input day's range in month"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="dateFromHiddenName" description="имя скрытого поля с началом диапазона (по-умолчанию dateFrom)"%>
<%@ attribute name="dateToHiddenName" description="имя скрытого поля с окончанием диапазона (по-умолчанию dateTo)"%>

<c:if test="${empty dateFromHiddenName}">
	<c:set var="dateFromHiddenName" value="dateFrom"/>
</c:if>
<c:if test="${empty dateToHiddenName}">
	<c:set var="dateToHiddenName" value="dateTo"/>
</c:if>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="datemonthdays" id="${uiid}"> 
	<input type="hidden" id="dateFrom" name="${dateFromHiddenName}" value="${form.param[dateFromHiddenName]}"/>
	<input type="hidden" id="dateTo" name="${dateToHiddenName}" value="${form.param[dateToHiddenName]}"/>
	
	<button class="btn-white icon" id="prev" onclick="return false"><i class="ti-angle-left"></i></button>
	<button class="btn-white" id="month" onclick="return false">&nbsp;</button>
	<button class="btn-white icon" id="next" onclick="return false"><i class="ti-angle-right"></i></button>
	с
	<input type="text" id="dayFrom" maxlength="2" name="${dateFromHiddenName}Day" value="${form.param[dateFromHiddenName.concat('Day')]}"/>
	по
	<input type="text" id="dayTo" maxlength="2" name="${dateToHiddenName}Day" value="${form.param[dateToHiddenName.concat('Day')]}"/>
</div>

<script>
	$(function()
	{
		uiMonthDaysSelectInit( $('#${uiid}') );
	})
</script>