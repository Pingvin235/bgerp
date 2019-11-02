<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/user" styleClass="center1020">
	<input type="hidden" name="action" value="permsetUpdate" />
	
	<c:set var="permset" value="${form.response.data.permset}" scope="page" />
	<c:set var="grantedPermission" value="${form.response.data.grantedPermission}" scope="request" />
	<c:set var="allPermissions" value="${form.response.data.allPermissions}" scope="request" />

	<c:set var="uiidSample" value="${u:uiid()}"/>
	<c:set var="uiidTo" value="${u:uiid()}"/>

	<div class="in-inline-block in-va-top">
		<div style="width: 50%;">
			<h2>ID</h2>
			
			<div id="${uiidSample}">
				<input type="text" name="id" style="width: 100%" value="${permset.id}" disabled="disabled"/>
				
				<h2>Название</h2>
				<input type="text" name="title" style="width: 100%" value="${permset.title}"/>
				
				<div style="display: none;">
					<h2>Роли</h2>
					<input type="text" name="roles" style="width: 100%" value="${permset.roles}"/>
				</div>
			</div>
		</div><%-- 
	--%><div style="width: 50%;" class="pl1">
			<h2>Комментарий</h2>
			<textarea name="comment" id="${uiidTo}" style="width: 100%; resize: none;">${permset.comment}</textarea>
		</div>
	</div>
		
	<u:sc>	
		<c:set var="selectorSample" value="#${uiidSample}"/>
		<c:set var="selectorTo" value="#${uiidTo}"/>
		<%@ include file="/WEB-INF/jspf/same_height.jsp"%>
	</u:sc>		
		
	<div class="in-inline-block mt1 mb1">
		<div style="width: 50%;">
			<h2>Права</h2>
			<div style="width:100%; height: 500px;">
				<c:set var="permissionTreeId" value="${u:uiid()}"/>
				<ul id="${permissionTreeId}" class="layout-height-rest" style="overflow: auto;">
					<c:forEach var="tree" items="${allPermissions}">
						<c:set var="node" value="${tree}" scope="request" />
						<jsp:include page="../check_tree_item.jsp" />
					</c:forEach>
				</ul>
				
				<script>
					$( function() 
					{
						$("#${permissionTreeId}").Tree();
					} );															
				</script>
			</div>		
		</div><%-- 
	--%><div style="width: 50%;" class="pl1">
			<h2>Конфигурация</h2>
			<textarea name="permsetConfig" style="width:100%; height: 500px; resize: none;" wrap="off">${permset.config}</textarea>
		</div>
	</div>
	
	<c:set var="toPostNames" value="['config','userConfig']"/>
	<%@ include file="/WEB-INF/jspf/send_and_cancel_form.jsp"%>	
</html:form>

<c:set var="state" value="Редактор"/>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/setup.html#user"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>