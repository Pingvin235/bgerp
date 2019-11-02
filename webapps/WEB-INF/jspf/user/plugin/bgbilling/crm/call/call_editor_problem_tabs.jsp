<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<script>
     $(function() {
         var $editorProblemTabs = $( "#editorProblemTabs-${form.param.billingId}-${form.param.contractId}-Tabs" ).tabs( {spinner: '' , refreshButton: true} );

         <c:url var="url" value="empty.do">
            <c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/crm/call/call_editor_problem_text.jsp"/>
         	<c:param name="billingId" value="${form.param.billingId}"/>
         	<c:param name="contractId" value="${form.param.contractId}"/>
		  </c:url>
	      $editorProblemTabs.tabs( "add", "${url}", "Создать" );	      

	      <c:url var="url" value="plugin/bgbilling/proto/billingCrm.do">
		  	<c:param name="action" value="callCurrentProblemList"/>	
		  	<c:param name="billingId" value="${form.param.billingId}"/>
         	<c:param name="contractId" value="${form.param.contractId}"/>
		  </c:url>
	      $editorProblemTabs.tabs( "add", "${url}", "Привязать" );	      
     });
</script>

<div id="editorProblemTabs-${form.param.billingId}-${form.param.contractId}-Tabs">
	<ul></ul>
</div>
