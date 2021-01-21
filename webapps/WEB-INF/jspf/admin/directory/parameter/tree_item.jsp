<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="nodeId" value="${node.parentId}-${node.id}"/> 

<ul>
	<li class="select_node" id="${nodeId}">
		<label><input name="value" type="hidden" value="${node.parentId}:${node.id}"/>${node.title}</label>
		
		<c:set var="addDialogId" value="${u:uiid()}"/>
		<span onclick="$('#${addDialogId}').dialog();" style="cursor:pointer;">(+)</span>
		<span onclick="$('li[id=${nodeId}]').remove();" style="cursor:pointer;">(-)</span>
		<div id="${addDialogId}" title="Родительский элимент - ${node.title}" style="display:none;overflow:auto;">
			<textarea id="${textareaId}" style="width:100%;height:200px;">${node.title} - </textarea><br/>
			<input value="OK" type="button" onclick="">
			<input value="${l.l('Отмена')}" type="button" onclick="$('#${addDialogId}').dialog( 'close' );">
		</div>
		
		<c:choose>
			<c:when test="${not empty node.children}">
					<c:forEach var="child" items="${node.children}"> 
						<c:set var="node" value="${child}" scope="request"/>
						<c:set var="parent" value="${node}" scope="request"/>
						<jsp:include page="tree_item.jsp"/>
					</c:forEach>
			</c:when>
		</c:choose>
	</li>
</ul>