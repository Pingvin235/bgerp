<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<script>
	$(function()
	{
		var $tabs = $("#${uiid}").tabs();

		<c:url var="url" value="plugin/bgbilling/proto/ipn.do">
			<c:param name="action" value="rangeList"/>
			<c:param name="billingId" value="${form.param.billingId}"/>
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="moduleId" value="${form.param.moduleId}"/>
		</c:url>
		$tabs.tabs( "add", "${url}", "Адреса" );

		<c:url var="url" value="plugin/bgbilling/proto/ipn.do">
			<c:param name="action" value="gateStatus"/>
			<c:param name="billingId" value="${form.param.billingId}"/>
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="moduleId" value="${form.param.moduleId}"/>
		</c:url>
		$tabs.tabs( "add", "${url}", "Шлюзы" );

		<c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
			<c:param name="action" value="serviceList"/>
			<c:param name="billingId" value="${form.param.billingId}"/>
			<c:param name="contractId" value="${form.param.contractId}"/>
			<c:param name="moduleId" value="${form.param.moduleId}"/>
		</c:url>
		$tabs.tabs( "add", "${url}", "Услуги" );
	})
</script>

<div id="${uiid}" style="height: 100%;">
	<ul></ul>
</div>

<script>
	$(function()
	{
		$('#${uiid} div.ui-tabs-panel').addClass( 'layout-height-rest' );
	})
</script>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>