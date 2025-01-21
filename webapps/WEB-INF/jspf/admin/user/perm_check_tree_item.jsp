<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not node.allowAll}">
	<c:set var="selected">
		<c:if test="${not empty node.action and grantedPermission.containsKey(node.action)}">checked='true'</c:if>
	</c:set>

	<li class="select_node ${selected}">
		<label class="ml05"><input name="permAction" type="checkbox" ${selected} value="${node.action}" style="display: none;"/>${node.title}</label>

		<%-- config --%>
		<c:if test="${not empty node.description}">
			<u:sc>
				<c:set var="permConf" value="${grantedPermission[node.action]}" />

				<c:set var="configParamId" value="${u:uiid()}"/>
				<c:set var="configDialogId" value="${u:uiid()}"/>
				<c:set var="linkId" value="${u:uiid()}"/>
				<c:set var="textareaId" value="${u:uiid()}"/>

				<input id="${configParamId}" name="permConfig" type="hidden" value="${node.action}#${permConf.getDataString()}"/>

				<span id="${linkId}" onclick="$('#${configDialogId}').dialog();" style="cursor:pointer;">[${permConf.getDataString().trim()}]</span>

				<div id="${configDialogId}" title="${node.title}" style="display:none; overflow:auto;">
					${node.description}
					<textarea id="${textareaId}" class="mt1 mb1 w100p" style="height:200px;">${permConf.getDataString().trim()}</textarea>
					<br/>
					<button class="btn-white" onclick="
						$('#${configParamId}').val('${node.action}' + '#' + $('#${textareaId}').val());
						$('#${linkId}').html( '[' + $('#${textareaId}').val() + ']');
						$('#${configDialogId}').dialog('close');">OK</button>
					<button class="btn-white ml1" onclick="$('#${configDialogId}').dialog('close');">${l.l("Cancel")}</button>
				</div>
			</u:sc>
		</c:if>

		<c:if test="${not empty node.children}">
			<ul>
				<c:forEach var="child" items="${node.children}">
					<c:set var="node" value="${child}" scope="request"/>
					<jsp:include page="perm_check_tree_item.jsp" />
				</c:forEach>
			</ul>
		</c:if>
	</li>
</c:if>