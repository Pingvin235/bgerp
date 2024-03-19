<%@ tag pageEncoding="UTF-8" description="Button"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
	Icons: https://themify.me/themify-icons
--%>

<%@ attribute name="type" required="true" description="Type of the button"%>
<%@ attribute name="onclick" description="onclick JS"%>
<%@ attribute name="id" description="CSS ID"%>
<%@ attribute name="styleClass" description="Button CSS class(es)"%>
<%@ attribute name="title" description="Custom title attribute"%>

<c:choose>
	<c:when test="${type eq 'ok'}">
		<button type="button" id="${id}" class="btn-grey ${styleClass}" onclick="${onclick}; return false;">OK</button>
	</c:when>
	<c:when test="${type eq 'cancel'}">
		<button type="button" id="${id}" class="btn-white ${styleClass}" onclick="${onclick}; return false;">${l.l('Cancel')}</button>
	</c:when>
	<c:when test="${type eq 'add'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Добавить')}" class="btn-green icon ${styleClass}" onclick="${onclick}; return false;"><i class="ti-plus"></i></button>
	</c:when>
	<c:when test="${type eq 'edit'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Редактировать')}" class="btn-white icon ${styleClass}" onclick="${onclick}; return false;"><i class="ti-pencil"></i></button>
	</c:when>
	<c:when test="${type eq 'del'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Удалить')}" class="btn-white icon ${styleClass}"
			onclick="if ($$.confirm.del()) { ${onclick} }; return false;"><i class="ti-trash"></i></button>
	</c:when>
	<c:when test="${type eq 'cut'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Вырезать')}" class="btn-white icon ${styleClass}" onclick="${onclick}; return false;"><i class="ti-cut"></i></button>
	</c:when>
	<c:when test="${type eq 'out'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Вывести')}" class="btn-grey icon ${styleClass}" onclick="${onclick}; return false;"><i class="ti-control-play"></i></button>
	</c:when>
	<c:when test="${type eq 'run'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Run')}" class="btn-grey icon ${styleClass}" onclick="${onclick}; return false;"><i class="ti-control-play"></i></button>
	</c:when>
	<c:when test="${type eq 'more'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Больше')}" class="btn-white icon ${styleClass}" onclick="${onclick}; return false;"><i class="ti-more"></i></button>
	</c:when>
	<c:when test="${type eq 'close'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Close')}" class="btn-white icon ${styleClass}" onclick="${onclick}; return false;"><i class="ti-close"></i></button>
	</c:when>
	<c:when test="${type eq 'clear'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Очистить')}" class="btn-white icon ${styleClass}" onclick="${onclick}; return false;"><i class="ti-close"></i></button>
	</c:when>
	<c:when test="${type eq 'back'}">
		<button type="button" id="${id}" title="${not empty title ? title : l.l('Close')}" class="btn-white icon ${styleClass}" onclick="${onclick}; return false;"><i class="ti-arrow-left"></i></button>
	</c:when>
	<c:when test="${type eq 'reply'}">
		<button type="button" id="${id}" title="${l.l('Ответить')}" class="btn-white icon ${styleClass}" style="transform: scaleY(-1);" onclick="${onclick}; return false;"><i class="ti-back-left"></i></button>
	</c:when>
</c:choose>
