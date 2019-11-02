<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="padding-left: ${10 * level}px; width: 100%;">
	<c:choose>
		<c:when test="${not empty node.childList}">
			<span id="${node.id}_title" onclick="$(this.parentNode).find( '#${node.id}_childs' ).toggle();">
				<img border="0" src="/img/folder.gif" alt="Node"/>&nbsp;${node.host}:${node.port} ${node.type} ${node.address} [${node.comment}]
			</span>
			<div id="${node.id}_childs" style="display: none;">
				<c:forEach var="child" items="${node.childList}"> 
					<c:set var="level" value="${level + 1}" scope="request"/>
					<c:set var="node" value="${child}" scope="request"/>
					<jsp:include page="gate_select_item.jsp"/>
					<c:set var="level" value="${level - 1}" scope="request"/>
				</c:forEach>
			</div>
		</c:when>
		<c:otherwise>
			<c:set var="script">
				var $form = $(this).closest('form');
				$form[0].gateId.value = ${node.id};
				$form[0].gateTypeId.value = ${node.typeId};
				$form.find( 'span' ).css( 'font-weight', '' ).css( 'color', '' );
				$(this).css( 'font-weight', 'bold' ).css( 'color', 'blue' );
			</c:set>
					
			<span id="${node.id}_title" onclick="${script}">
				<img border="0" style="cursor: pointer;" src="/img/page.png" alt="Type"/>&nbsp;${node.host}:${node.port} ${node.type} ${node.address} [${node.comment}]
			</span>
		</c:otherwise>
	</c:choose>
</div>