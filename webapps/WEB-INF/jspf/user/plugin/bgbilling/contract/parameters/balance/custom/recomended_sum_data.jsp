<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<%@ page language="java" import="java.lang.*, java.util.*, java.text.*" %>
<%@ page import="ru.bgcrm.plugin.ufanet.dao.RecommendedSum" %>
<%@page import="ru.bgcrm.util.Utils"%>
<%@page import="ru.bgcrm.util.TimeUtils"%>


<c:set var="billingId" value="${form.param.billingId}"/>
<c:set var="contractId" value="${form.param.contractId}"/>
<c:set var="dateFrom" value="${form.param.dateFrom}"/>
<c:set var="dateTo" value="${form.param.dateTo}"/>

<table class="data hl">
	<c:forEach var="item" items='<%= new RecommendedSum().getInstance( String.valueOf( pageContext.getAttribute("billingId")),
	                                                             Utils.parseInt( String.valueOf(pageContext.getAttribute("contractId"))),
	                                                             TimeUtils.parse(  String.valueOf(pageContext.getAttribute("dateTo")), TimeUtils.PATTERN_DDMMYYYY )
	                                                             ).entrySet()%>'>
		<tr>
			<td class="box" nowrap="nowrap">${item.getKey()}</td>
			<td class="box" nowrap="nowrap">${item.getValue()}</td>
		</tr>
	</c:forEach>
</table>
