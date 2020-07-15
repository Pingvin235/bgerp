<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<script>
	$(function()
	{
		var $tabs = $("#${uiid}").tabs();

		<c:url var="url" value="/user/plugin/bgbilling/proto/bill.do">
			<c:param name="action" value="attributeList"/>
			<c:param name="billingId" value="${form.param.billingId}"/>
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="moduleId" value="${form.param.moduleId}"/>
		</c:url>
		$tabs.tabs( "add", "${url}", "Реквизиты" );

		<c:url var="url" value="/user/plugin/bgbilling/proto/bill.do">
			<c:param name="action" value="docTypeList"/>
			<c:param name="billingId" value="${form.param.billingId}"/>
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="moduleId" value="${form.param.moduleId}"/>
		</c:url>
		$tabs.tabs( "add", "${url}", "Типы документов" );
	})
</script>

<div id="${uiid}">
	<ul></ul>
</div>