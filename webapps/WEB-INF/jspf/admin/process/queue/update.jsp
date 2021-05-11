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

			<h2>${l.l('Название')}</h2>
			<input type="text" name="title" style="width: 100%" value="${queue.title}"/>

			<h2>${l.l('Типы процессов')}</h2>

			<c:set var="processTypeIds" value="${queue.processTypeIds}" scope="request"/>

			<c:set var="treeId" value="${u:uiid()}"/>
			<ul id="${treeId}" class="layout-height-rest" style="overflow: auto;">
				<c:forEach var="node" items="${typeTreeRoot.childs}">
					<c:set var="node" value="${node}" scope="request"/>
					<jsp:include page="../process_type_check_tree_item.jsp"/>
				</c:forEach>
			</ul>

			<script>
				$(function () {
					$("#${treeId}").Tree();
				});
			</script>
		</div><%--
	--%><div style="width: 50%; height: 100%;" class="pl1">
			<h2>${l.l('Конфигурация')}</h2>
			<c:set var="taUiid" value="${u:uiid()}"/>
			<textarea id="${taUiid}" name="config" class="layout-height-rest" style="width: 100%; resize: none;" wrap="off">${queue.config}</textarea>
		</div>
	</div>

	<div class="mt1">
		<c:set var="saveCommand" value="$$.ajax.post(this.form, {toPostNames: ['config']})"/>
		<c:set var="returnCommand" value="$$.ajax.load('${form.returnUrl}', $$.shell.$content(this))"/>

		<button type="button" class="btn-grey mr1"  onclick="${saveCommand}.done(${returnCommand})">OK</button>
		<button type="button" class="btn-white" onclick="${returnCommand}">${l.l('Отмена')}</button>

		<c:url var="editUrl" value="/admin/process.do">
			<c:param name="action" value="queueGet"/>
			<c:param name="id" value="${form.id}"/>
			<c:param name="returnUrl" value="${form.returnUrl}"/>
		</c:url>

		<button type="button" class="btn-grey" onclick="
				${saveCommand}.done(() => { 
					$$.ajax.load('${editUrl}', $(this.form).parent())
				})"
			style="float: right;" title="${l.l('Сохранить без выхода из редактора')}">Сохранить</button>
	</div>
</html:form>

<script>
	$(function () {
		$$.ui.codeMirror('${taUiid}');
	});
</script>

<shell:state ltext="Редактор" help="kernel/process/queue.html#setup"/>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>