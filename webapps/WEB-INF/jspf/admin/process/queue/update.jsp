<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="queue" value="${form.response.data.queue}"/>
<c:set var="formUiid" value="${u:uiid()}"/>

<html:form action="admin/process" styleId="${formUiid}" styleClass="center1020">
	<input type="hidden" name="action" value="queueUpdate"/>
	
	<c:set var="lastModifyUiid" value="${u:uiid()}"/>
	<div id="${lastModifyUiid}">	
		<c:set var="lastModifyObject" value="${queue}"/>
		<%@ include file="/WEB-INF/jspf/last_modify_hiddens.jsp"%>
	</div>	
	
	<div class="in-inline-block" style="height: 700px;">
		<div style="width: 50%; height: 100%;">
			<h2>ID</h2>
			<input type="text" name="id" style="width: 100%;" value="${queue.id}" disabled="disabled"/>
			
			<h2>Название</h2>
			<input type="text" name="title" style="width: 100%" value="${queue.title}"/>
			
			<h2>Типы процессов</h2>
			
			<c:set var="processTypeIds" value="${queue.processTypeIds}" scope="request"/>
			
			<c:set var="treeId" value="${u:uiid()}"/>
			<ul id="${treeId}" class="layout-height-rest" style="overflow: auto;">
				<c:forEach var="node" items="${typeTreeRoot.childs}">
					<c:set var="node" value="${node}" scope="request"/>
					<jsp:include page="../process_type_check_tree_item.jsp"/>
				</c:forEach>
			</ul>
			
			<script>
				$( function() 
				{
					$("#${treeId}").Tree();
				} );															
			</script>			
		</div><%--
	--%><div style="width: 50%; height: 100%;" class="pl1">
			<h2>Конфигурация</h2>
			<c:set var="taUiid" value="${u:uiid()}"/>
			<textarea id="${taUiid}" name="config" class="layout-height-rest" style="width: 100%; resize: none;" wrap="off">${queue.config}</textarea>
		</div>
	</div>
	
	<div class="mt1">
		<c:set var="saveCommand" value="sendAJAXCommand( formUrl( $('#${formUiid}') ), ['config'] )"/>
	
		<button type="button" class="btn-grey mr1" onclick="if( ${saveCommand } ){ openUrlContent( '${form.returnUrl}' ) }">ОК</button> 
		<button type="button" class="btn-grey" onclick="openUrlContent( '${form.returnUrl}' )">Отмена</button>
		
		<c:set var="saveScript">
			var result = ${saveCommand}; 
			if( result ){ 
			   updateLastModify( result.data.queue, $('#${lastModifyUiid}') );
			   this.form.id.value = result.data.queue.id;
			   bgerp.ui.unmarkChanged($('#${taUiid}')); 
			}
		</c:set>
		
		<button type="button" class="btn-grey" onclick="${saveScript}" style="float: right;" title="Сохранить без выхода из редактора">Сохранить</button>
	</div>
</html:form>

<script>
	$(function () {
		$$.ui.markChanged($('#${taUiid}'));
	});
</script>

<c:set var="state" value="Редактор"/>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/process/queue.html#setup"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>