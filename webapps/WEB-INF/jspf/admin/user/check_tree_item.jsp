<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not node.allowAll}">
	<c:set var="configDialogId" value="${u:uiid()}"/>
	<c:set var="configParamId" value="${u:uiid()}"/>
	<c:set var="textareaId" value="${u:uiid()}"/>
	<c:set var="linkId" value="${u:uiid()}"/>
	<c:set var="permConf" value="${grantedPermission[node.action]}" />

	<c:set var="selected">
		<c:if test="${not empty node.action and grantedPermission.containsKey(node.action)}">checked='true'</c:if>
	</c:set>

	<li class="select_node ${selected}">
		<label><input name="dataPermissionType" type="checkbox" ${selected} value="${node.action}"/>${node.title}</label>
		<c:if test="${not empty node.description}">
			<input id="${configParamId}" name="config" type="hidden" value="${node.action}#${permConf.getDataString()}"/>
		</c:if>
		<c:if test="${empty node.description}">
			<input id="${configParamId}" name="config" type="hidden" value=""/>
		</c:if>

		<%-- CONFIG --%>
		<c:if test="${not empty node.description}">
			<c:set var="stringPermConf">[]</c:set>
			<c:if test="${permConf.size() gt 0}">
				<c:set var="stringPermConf">[${permConf.getDataString()}]</c:set>
			</c:if>

			<span id="${linkId}" onclick="$('#${configDialogId}').dialog();" style="cursor:pointer;">${stringPermConf}</span>

			<div id="${configDialogId}" title="${node.title}" style="display:none;overflow:auto;">
				${node.description}
				<textarea id="${textareaId}" class="mt1 mb1" style="width:100%;height:200px;">${permConf.getDataString()}</textarea><br/>
				<button class="btn-white" onclick="$('#${configParamId}').val( '${node.action}' + '#' + $('#${textareaId}').val() ); $('#${linkId}').html( '[' + $('#${textareaId}').val() + ']' ); $('#${configDialogId}').dialog('close');">OK</button>
				<button class="btn-white ml1" onclick="$('#${configDialogId}').dialog( 'close' );">${l.l("Отмена")}</button>
			</div>
		</c:if>

		<c:set var="selected" value=""/>
		<c:set var="permConf" value=""/>

		<c:if test="${not empty node.children}">
			<ul>
				<c:forEach var="child" items="${node.children}">
					<c:set var="node" value="${child}" scope="request" />
					<jsp:include page="check_tree_item.jsp" />
				</c:forEach>
			</ul>
		</c:if>
	</li>
</c:if>