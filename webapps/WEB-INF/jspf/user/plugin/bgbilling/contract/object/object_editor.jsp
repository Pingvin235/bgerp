<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<h2>Редактор</h2>

<input type="button" value="Назад к списку объектов" onclick="$$.ajax.load( '${form.param.returnUrl}',  $('#${form.param.billingId }-${form.param.contractId }-objectInfo'))"/>
</br>

<form action="/user/plugin/bgbilling/proto/contract.do">
	<c:set var="object" value="${frd.object}"/>

	<input type="hidden" name="action" value="updateContractObject"/>
	<input type="hidden" name="objectId" value="${object.id }"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>
	<input type="hidden" name="typeId" value="${object.typeId}"/>

	<div style="display:table;width:100%;white-space:nowrap">
		<div style="display:table-cell;" nowrap="nowrap">
			Период c
			<ui:date-time paramName="dateFrom" value="${object.dateFrom}"/>
			по
			<ui:date-time paramName="dateTo" value="${object.dateTo}"/>

			Название:
		</div>

		<div style="display:table-cell;width:100%">
			<input type="text" style="width:100%" name="title" value="${object.title}"/>
		</div>

		<div style="display:table-cell">
			Тип:
		</div>

		<div style="display:table-cell">
			<input type="text" style="width:100%" value="${form.param.objectType}" readOnly/>
		</div>

		<div style="display:table-cell">
			<input type="button" value="Применить" onclick="if$$.ajax.post(this.form).done(() => alert('Изменения произведены успешно!'))"/>
		</div>
	</div>
</form>

<script>
     $(function() {
         var $objectEditorTabs = $( "#objectEditorTabs-${form.param.billingId}-${form.param.contractId}-Tabs" ).tabs( {spinner: '', refreshButton:true} );

         <c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
		  	<c:param name="action" value="contractObjectParameterList"/>
		  	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  	<c:param name="objectId" value="${form.param.objectId}"/>
		  	<c:param name="returnUrl" value="${form.param.returnUrl}"/>
		  </c:url>
	      $objectEditorTabs.tabs( "add", "${url}", "Параметры объекта" );

	      <c:url var="url" value="/user/empty.do">
		  	<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/contract/parameters/object/object_module_list.jsp"/>
		  	<c:param name="billingId" value="${form.param.billingId}"/>
		  	<c:param name="contractId" value="${form.param.contractId}"/>
		  	<c:param name="objectId" value="${form.param.objectId}"/>
		  	<c:param name="returnUrl" value="${form.param.returnUrl}"/>
		  </c:url>
		  $objectEditorTabs.tabs( "add", "${url}", "Модули объекты" );
     });
</script>

<div id="objectEditorTabs-${form.param.billingId}-${form.param.contractId}-Tabs">
	<ul></ul>
</div>