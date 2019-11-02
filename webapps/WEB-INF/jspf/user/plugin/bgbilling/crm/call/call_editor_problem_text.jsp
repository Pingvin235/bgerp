<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<script>
   $('#editorProblemTabs-${form.param.billingId}-${form.param.contractId}-Tabs').bind("tabsselect", function(e, ui) {
       if ("Создать" == $(ui.tab).text())
     	 {
     	  	$('#${form.param.billingId}-${form.param.contractId}-createCallform select[name=linkProblemId]').children().removeAttr("selected");
     	 }
   });
</script>
<div class="box" style="overflow: auto; width: inherit; height: 250px;">
	<textarea rows="10" cols="300" style="width:100%; height:100%;" 
	onChange="$('#${form.param.billingId}-${form.param.contractId}-createCallform input[name=problemComment]').val( $(this).val() );"/>
</div>