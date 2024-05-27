<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="url" value="/user/plugin/bgbilling/contract.do">
	<c:param name="method">customerContractList</c:param>
	<c:param name="customerId">${customer.id}</c:param>
</c:url>

$tabs.tabs("add", "${url}", "Договоры", " id='bgbilling-contracts'", 2);
