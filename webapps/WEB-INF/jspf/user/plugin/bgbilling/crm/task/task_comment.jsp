<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>





<div class="box" style="overflow: auto; width: inherit; height: 240px;">
	<textarea rows="10" cols="300" style="width:100%; height:100%;" 
	onChange="$('#${form.param.billingId}-${form.param.contractId}-createTaskform input[name=taskComment]').val( $(this).val() );">${form.param.taskComment}</textarea>
</div>