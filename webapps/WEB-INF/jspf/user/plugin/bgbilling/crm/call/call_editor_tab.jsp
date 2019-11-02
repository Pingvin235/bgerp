<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>


<h2>Редактор</h2>

<script>
     $(function() {
         var $callEditorTabs = $( "#callEditorTabs-${form.param.billingId}-${form.param.contractId}-Tabs" ).tabs( {spinner: '' , refreshButton: true} );

         <c:url var="url" value="plugin/bgbilling/proto/billingCrm.do">
		  	<c:param name="action" value="callTypeList"/>	
		  	<c:param name="billingId" value="${form.param.billingId}"/>	
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  </c:url>
	      $callEditorTabs.tabs( "add", "${url}", "Тип звонка" );	      

	      <c:url var="url" value="plugin/bgbilling/proto/billingCrm.do">
		  	<c:param name="action" value="registerGroupList"/>	
		  	<c:param name="billingId" value="${form.param.billingId}"/>		
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  	<c:param name="multiline" value="multiline size='14'"/>
		  </c:url>
	      $callEditorTabs.tabs( "add", "${url}", "Группа" );
	      $callEditorTabs.tabs( "load" , 1);

	      <c:url var="url" value="empty.do">
	        <c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/crm/call/call_editor_problem_tabs.jsp"/>
		  	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>	
		  </c:url>
		  $callEditorTabs.tabs( "add", "${url}", "Проблема" );		  
     });
</script>
<div class="tableIndent">
	<form action="plugin/bgbilling/proto/billingCrm.do" id="${form.param.billingId}-${form.param.contractId}-createCallForm">
		<div id="callEditorTabs-${form.param.billingId}-${form.param.contractId}-Tabs">
			<ul></ul>
		</div>
		<c:set var="crmTabs" value="crmTabs-${form.param.billingId}-${form.param.contractId}-Tabs"/>
		<input type="hidden" name="action" value="createRegisterCall"/>
		<input type="hidden" name="billingId" value="${form.param.billingId }" />
		<input type="hidden" name="contractId" value="${form.param.contractId }" />
		<input type="hidden" name="comment" />
		<input type="hidden" name="problemComment" />
		<input type="button" value="OK" onClick="openUrl( formUrl( $('#${form.param.billingId}-${form.param.contractId}-createCallForm') ) ); 
												$('#${crmTabs}').tabs('load',$('#${crmTabs}').tabs('option', 'selected'));"/>
		<input type="button" value="Отмена"	onClick="$('#${crmTabs}').tabs('load',$('#${crmTabs}').tabs('option', 'selected'));" />
	</form>
</div>

