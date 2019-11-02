<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script>
     $(function() {
         var $crmTabs = $( "#crmTabs-${param.billingId}-${param.contractId}-Tabs" ).tabs( {spinner: '' , refreshButton: true} );

         <c:url var="url" value="plugin/bgbilling/proto/billingCrm.do">
		  	<c:param name="action" value="callList"/>	
		  	<c:param name="billingId" value="${param.billingId}"/>	
		  	<c:param name="contractId" value="${param.contractId}"/>	
		  </c:url>
	      $crmTabs.tabs( "add", "${url}", "Звонки" );	      

	      <c:url var="url" value="plugin/bgbilling/proto/billingCrm.do">
		  	<c:param name="action" value="taskList"/>	
		  	<c:param name="billingId" value="${param.billingId}"/>	
		  	<c:param name="contractId" value="${param.contractId}"/>	
		  </c:url>
	      $crmTabs.tabs( "add", "${url}", "Задачи" );	      

	      <%--
	      <c:url var="url" value="#UNDEF">		
		  </c:url>
		  $crmTabs.tabs( "add", "${url}", "Работы" );
		  --%>		  
     });
</script>

<div id="crmTabs-${param.billingId}-${param.contractId}-Tabs">
	<ul></ul>
</div>