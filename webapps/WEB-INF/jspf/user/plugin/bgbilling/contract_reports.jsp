<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<script>
$(function () {
	const $tabs = $('#${uiid}').tabs();

	<c:forEach var="item" items="${contract.moduleList}">
		<c:choose>
			<c:when test="${item.clientPackage eq 'ru.bitel.bgbilling.modules.bill.client' or item.clientPackage eq 'bitel.billing.module.services.bill'}">
				$tabs.tabs('add', '/user/plugin/bgbilling/proto/bill.do?action=documentList&billingId=${billingId}&contractId=${contractId}&moduleId=${item.moduleId}', '${item.title}');
			</c:when>
			<c:when test="${item.clientPackage eq 'ru.bitel.bgbilling.modules.inet.api.client' or item.clientPackage eq 'ru.bitel.bgbilling.modules.inet.client'}">
				$tabs.tabs('add', '/user/empty.do?forwardFile=/WEB-INF/jspf/user/plugin/bgbilling/inet/contract_report.jsp&billingId=${billingId}&contractId=${contractId}&moduleId=${item.moduleId}', '${item.title}');
			</c:when>
			<c:otherwise></c:otherwise>
		</c:choose>
	</c:forEach>
})
</script>

<div id="${uiid}">
	<ul></ul>
</div>