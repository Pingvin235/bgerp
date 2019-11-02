<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
<c:if test="${not empty setup['bgbilling:commonContract.addressParamId'] }">
	<c:url var="url" value="plugin/bgbilling/commonContract.do">
		<c:param name="action">commonContractList</c:param>
		<c:param name="customerId">${customer.id}</c:param>	
	</c:url>
	
	$tabs.tabs( "add", "${url}", "Единые договоры" );
</c:if>
--%>	

<c:url var="url" value="plugin/bgbilling/contract.do">
	<c:param name="action">customerContractList</c:param>
	<c:param name="customerId">${customer.id}</c:param>
</c:url>

$tabs.tabs( "add", "${url}", "Договоры", " id='bgbilling-contracts'", 2 );

