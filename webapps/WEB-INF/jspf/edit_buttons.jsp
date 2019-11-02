<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty addUrl}">
	<button type="button" class="btn-white btn-small" onclick="openUrlContent('${addUrl}')" title="Добавить">+</button>
	<c:remove var="addUrl"/>
</c:if>

<c:if test="${not empty addCommand}">
	<button type="button" class="btn-white btn-small" onclick="${addCommand}" value="+" title="Добавить">+</button>
	<c:remove var="addCommand"/>
</c:if>

<c:if test="${not empty editUrl}">
	<button type="button" class="btn-white btn-small" onclick="openUrlContent('${editUrl}')" title="Редактировать">*</button>
	<c:remove var="editUrl"/>
</c:if>

<c:if test="${not empty editCommand}">
	<button type="button" class="btn-white btn-small" onclick="${editCommand}" title="Редактировать">*</button>
	<c:remove var="editCommand"/>
</c:if>

<c:if test="${not empty deleteUrl}">
	<button type="button" class="btn-white btn-small" onclick="if( confirm( 'Вы уверены, что хотите удалить?' ) ){ openUrlContent('${deleteUrl}') }" title="Удалить">X</button>
	<c:remove var="deleteUrl"/>
</c:if>

<c:if test="${not empty deleteAjaxUrl}">
	<button type="button" class="btn-white btn-small" onclick="if( confirm( 'Вы уверены, что хотите удалить?' ) && sendAJAXCommand( '${deleteAjaxUrl}' ) ) { ${deleteAjaxCommandAfter} }" title="Удалить">X</button>
	<c:remove var="deleteAjaxUrl"/>
</c:if>

<%-- FIXME: deleteCommand должен работать идентично editCommand --%>
<c:if test="${not empty deleteCommand}">
	<button type="button" class="btn-white btn-small" onclick="if( confirm( 'Вы уверены, что хотите удалить?' ) && sendAJAXCommand( ${deleteCommand} ) ) { ${deleteAjaxCommandAfter} }" title="Удалить">X</button>
	<c:remove var="deleteCommand"/>
</c:if>



