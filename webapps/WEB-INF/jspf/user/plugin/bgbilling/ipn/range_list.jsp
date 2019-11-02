<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="baseUrl" value="plugin/bgbilling/proto/ipn.do">
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<c:set var="list" value="${form.response.data.rangeList}"/>
<c:set var="columnTitle" value="Диапазон"/>
<%@ include file="range_list_items.jsp"%>

<div class="mt1"></div>

<c:url var="baseUrl" value="${baseUrl}">
	<c:param name="mode" value="net"/>
</c:url>

<c:set var="list" value="${form.response.data.netList}"/>
<c:set var="columnTitle" value="Сеть"/>
<%@ include file="range_list_items.jsp"%>