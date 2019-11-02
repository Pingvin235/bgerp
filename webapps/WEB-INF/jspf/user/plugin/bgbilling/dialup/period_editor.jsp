<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="period" value="${form.response.data.period}"/>
<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="updateCommand" value="openUrlToParent('${form.requestUrl}',$('#${uiid}'));"/>

<h1>Редактирование учётного периода</h1>

<html:form action="/user/plugin/bgbilling/proto/dialup">
	<input type="hidden" name="action" value="updatePeriod"/>
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>
			
	Период c
  	<c:set var="editable" value="true"/>
	<input type="text" name="dateFrom" value="${u:formatDate( period.startDate, 'ymd' )}" id="${uiid}-dateFrom"/>	
	<c:set var="selector" value="#${uiid}-dateFrom"/>	
	<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
	по
	<c:set var="editable" value="true"/>
	<input type="text" name="dateTo" value="${u:formatDate( period.endDate, 'ymd' )}" id="${uiid}-dateTo" />
	<c:set var="selector" value="#${uiid}-dateTo"/>	
	<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
	
	<%@ include file="/WEB-INF/jspf/ok_cancel_in_form.jsp"%>
</html:form>