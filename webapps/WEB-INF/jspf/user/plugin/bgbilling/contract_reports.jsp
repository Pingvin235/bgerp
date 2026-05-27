<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<script>
$(function () {
	const $tabs = $('#${uiid}').tabs();

	<c:forEach var="item" items="${contract.moduleList}">
		<c:choose>
			<c:when test="${item.clientPackage eq 'ru.bitel.bgbilling.modules.bill.client' or item.clientPackage eq 'bitel.billing.module.services.bill'}">
				<p:check action="/user/plugin/bgbilling/proto/bill:documentList">
					$tabs.tabs('add', '/user/plugin/bgbilling/proto/bill.do?method=documentList&billingId=${billingId}&contractId=${contractId}&moduleId=${item.moduleId}', '${item.title}');
				</p:check>
			</c:when>
			<c:when test="${item.clientPackage eq 'ru.bitel.bgbilling.modules.inet.api.client' or item.clientPackage eq 'ru.bitel.bgbilling.modules.inet.client'}">
				<p:check action="/user/plugin/bgbilling/proto/inet:null">
					$tabs.tabs('add', '/user/plugin/bgbilling/proto/inet.do?billingId=${billingId}&contractId=${contractId}&moduleId=${item.moduleId}', '${item.title}');
				</p:check>
			</c:when>
			<c:otherwise></c:otherwise>
		</c:choose>
	</c:forEach>
})
</script>

<div id="${uiid}">
	<ul></ul>
</div>