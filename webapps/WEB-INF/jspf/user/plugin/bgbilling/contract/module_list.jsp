<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/> 

<div class="in-table-cell" id="${uiid}">
	<form style="width: 300; vertical-align: top;" action="plugin/bgbilling/proto/contract.do" id="selected">
		<input type="hidden" name="action" value="updateModules"/>
		<input type="hidden" name="command" value="del"/>
		<input type="hidden" name="billingId" value="${form.param.billingId}"/>
		<input type="hidden" name="contractId" value="${form.param.contractId}"/>		
	
		<h2>Выбранные модули</h2>
		<c:forEach var="item" items="${form.response.data.selectedList}">
			<div class="mb05"><input type="checkbox" name="moduleId" value="${item.id}"/>&nbsp;${item.title}</div>
		</c:forEach>
		
		<button type="button" class="btn-grey mb1" 
			onclick="if( confirm( 'Удаление модулей приведёт к удалению данных,\nвы уверены?' ) && sendAJAXCommand( formUrl( this.form ) ) ){ openUrlToParent('${form.requestUrl}',$('#${uiid}')); }">
			&gt;&gt;&gt;
		</button>
	</form>
	<form style="width: 300; vertical-align: top;" action="plugin/bgbilling/proto/contract.do" id="available">
		<input type="hidden" name="action" value="updateModules"/>
		<input type="hidden" name="command" value="add"/>
		<input type="hidden" name="billingId" value="${form.param.billingId}"/>
		<input type="hidden" name="contractId" value="${form.param.contractId}"/>
		
		<h2>Доступные модули</h2>
		<c:forEach var="item" items="${form.response.data.availableList}">
			<div class="mb05"><input type="checkbox" name="moduleId" value="${item.id}"/>&nbsp;${item.title}</div>
		</c:forEach>
		
		<button type="button" class="btn-grey"
			onclick="if( sendAJAXCommand( formUrl( this.form ) ) ){ openUrlToParent('${form.requestUrl}',$('#${uiid}')); }">&lt;&lt;&lt;</button>
	</form>
</div>

<%-- 
<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function()
	{
		$('#${contractTreeId} #treeTable tr.module').remove();
		var $insertPoint = $('#${contractTreeId} #treeTable tr#modules');
		
		var modules = "";
		$('#${uiid} #selected input[type=checkbox]').each( function()
		{
			var text = $(this).parent().text();
			modules += '<tr class="module">\<td colspan="2"><div class="pl2 row">' + text + "</div></td></tr>"; 
		});
		
		$insertPoint.after( modules );	
	})
</script>
--%>