<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${processType.properties.configMap['invoice:processShowInvoices'] eq 1}">
	<c:url var="url" value="/user/plugin/invoice/invoice.do">
		<c:param name="method" value="list" />
		<c:param name="processId" value="${process.id}" />
	</c:url>
	$tabs.tabs('add', "${url}", "${l.l("Счета")}");
</c:if>