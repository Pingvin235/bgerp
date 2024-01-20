<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="in-va-top" id="${uiid}">
	<div style="width: 50%; display: inline-block;">
		<h2>Типы счетов</h2>

		<c:set var="list" value="${frd.billTypeList}"/>
		<%@ include file="doc_type_list_items.jsp"%>
	</div><%--
--%><div style="width: 50%;  display: inline-block;" class="pl1">
		<h2>Типы счетов-фактур</h2>

		<c:set var="list" value="${frd.invoiceTypeList}"/>
		<%@ include file="doc_type_list_items.jsp"%>
	</div>
</div>