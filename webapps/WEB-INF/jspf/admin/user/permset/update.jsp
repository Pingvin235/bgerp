<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/admin/user" styleClass="center1020">
	<input type="hidden" name="method" value="permsetUpdate" />

	<c:set var="permset" value="${frd.permset}" scope="page" />
	<c:set var="grantedPermission" value="${frd.grantedPermission}" scope="request" />

	<c:set var="uiidSample" value="${u:uiid()}"/>
	<c:set var="uiidTo" value="${u:uiid()}"/>

	<div class="in-inline-block in-va-top">
		<div style="width: 50%;">
			<h2>ID</h2>

			<div id="${uiidSample}">
				<input type="text" name="id" style="width: 100%" value="${permset.id}" disabled="disabled"/>

				<h2>${l.l('Название')}</h2>
				<input type="text" name="title" style="width: 100%" value="${permset.title}"/>

				<div style="display: none;">
					<h2>${l.l('Роли')}</h2>
					<input type="text" name="roles" style="width: 100%" value="${permset.roles}"/>
				</div>
			</div>
		</div><%--
	--%><div style="width: 50%;" class="pl1">
			<h2>${l.l('Comment')}</h2>
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
			<h2>${l.l('Permissions')}</h2>
			<div style="height: 500px;">
				<c:set var="permissionTreeId" value="${u:uiid()}"/>
				<ul id="${permissionTreeId}" class="layout-height-rest" style="overflow: auto;">
					<c:forEach var="tree" items="${permTrees}">
						<c:set var="node" value="${tree}" scope="request" />
						<jsp:include page="../perm_check_tree_item.jsp" />
					</c:forEach>
				</ul>

				<script>
					$(function () {
						$("#${permissionTreeId}").Tree();
					});
				</script>
			</div>
		</div><%--
	--%><div style="width: 50%;" class="pl1">
			<h2>${l.l('Configuration')}</h2>
			<div style="height: 500px;">
				<c:set var="taUiid" value="${u:uiid()}"/>
				<textarea id="${taUiid}" name="permsetConfig" style="width: 100%; height: 100%; resize: none;" wrap="off">${permset.config}</textarea>
			</div>
		</div>
	</div>

	<ui:form-ok-cancel/>
</html:form>

<shell:state text="${l.l('Editor')}" help="kernel/setup.html#user"/>

<script>
	$$.ui.codeMirror('${taUiid}');
</script>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>