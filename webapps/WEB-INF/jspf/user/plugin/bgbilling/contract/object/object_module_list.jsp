<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script>
     $(function() {
		 var url = "/user/plugin/bgbilling/proto/contract.do?action=contractObjectModuleInfo";
		 var result = sendAJAXCommandWithParams( url, {"billingId" : '${form.param.billingId}', "objectId":'${form.param.objectId}' });
    	 
         var $objectModuleListTabs = $( "#objectModuleListTabs-${form.param.billingId}-${form.param.contractId}-Tabs" ).tabs( {spinner: '', refreshButton:true} );

         <c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
		  	<c:param name="action" value="contractObjectModuleSummaryTable"/>	
		  	<c:param name="billingId" value="${form.param.billingId}"/>	
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  	<c:param name="objectId" value="${form.param.objectId}"/>
		  	<c:param name="returnUrl" value="${form.param.returnUrl}"/>
		  </c:url>
	      $objectModuleListTabs.tabs( "add", "${url}", "Сводная таблица" );	      

	      for(var i=0; i< result.data.moduleInfo.moduleList.length; i++)
	    	{
		      <c:url var="url" value="empty.do">
			  </c:url>
			  $objectModuleListTabs.tabs( "add", "${url}", result.data.moduleInfo.moduleList[i].title);
	    	}
     });
</script>

<div id="objectModuleListTabs-${form.param.billingId}-${form.param.contractId}-Tabs">
	<ul></ul>
</div>