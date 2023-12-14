<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<%@ page language="java" import="java.lang.*, java.util.*, java.text.*" %>

<c:set var="billingId" value="${form.param.billingId}"/>
<c:set var="contractId" value="${form.param.contractId}"/>
<c:set var="user" value="${form.user}"/>

<c:set var="uiid" value="${u:uiid()}"/>

<jsp:useBean id="curdate" class="java.util.Date"/>
<c:set var="dateFrom" value="${tu.format(curdate, 'dd.MM.yyyy'})"/>

<c:set var="dateTo" value="${tu.format(tu.getEndMonth(curdate), 'dd.MM.yyyy'})"/>

<c:url var="url" value="empty.do">
	<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/contract/parameters/balance/custom/recomended_sum_data.jsp"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
</c:url>

	<c:set var="type" value="ymd"/>
	<b>Рекомендуемая сумма по </b></b>
	<ui:date-time id="${uiid}-dateTo" value="${dateTo}"/>

	<input type="button" value="Обновить" onclick="$$.ajax.load('${url}'+'&dateFrom='+$('#${uiid}-dateFrom').val()+'&dateTo='+$('#${uiid}-dateTo').val(), $('#${uiid}'));"/>

<div style="vertical-align:top;align:right;" id="${uiid}">
	<script>
		$$.ajax.load('${url}'+'&dateTo='+$('#${uiid}-dateTo').val(), $('#${uiid}'));
	</script>
</div>
