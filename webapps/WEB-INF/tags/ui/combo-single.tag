<%@ tag body-content="empty" pageEncoding="UTF-8" description="Drop down list with a single selection"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
You can use the following methods to set values:

1)
list - List<IdTitle> of elements
map - Map<Integer, IdTitle> of elements
available - List<Integer> of allowed values
TODO: could use the same method and use select_single availableIdList?

If 'available' is defined, then selection is done according to it with picking corresponding values from 'map'
Otherwise 'list' and its ordering are used.

2)
valuesHtml - HTML-text with values as li elements

You can set width either via 'style' or 'styleTextValue' or 'widthTextValue'
Use styleTextValue / widthTextValue in situations when you expect a long value inside your list.
--%>

<%@ attribute name="id" description="id of outer DIV, auto generated if not explicitly specified"%>
<%@ attribute name="hiddenName" description="hidden input name"%>
<%@ attribute name="prefixText" description="text prefix"%>
<%@ attribute name="value" description="hidden input's current value"%>
<%@ attribute name="style" description="outer DIV style"%>
<%@ attribute name="styleClass" description="outer DIV style"%>
<%@ attribute name="styleTextValue" description="current value's DIV style"%>
<%@ attribute name="widthTextValue" description="current value's block width"%>
<%@ attribute name="onSelect" description="JS, action to be performed on value selection"%>
<%@ attribute name="disable" description="disable edits (TODO: use another color)"%>
<%@ attribute name="showFilter" type="java.lang.Boolean" description="Enable/disable Filter"%>
<%@ attribute name="valuesHtml" description="HTML-text with values as li elements, refer to description inside tag"%>

<%@ attribute name="list" type="java.util.Collection" description="List of values, refer to description inside tag"%>
<%@ attribute name="map" type="java.util.Map" description="Map of values, refer to description inside tag"%>
<%@ attribute name="available" type="java.util.Collection" description="Set of allowed ids, refer to description inside tag"%>

<c:if test="${not empty widthTextValue}">
	<c:set var="styleTextValue">min-width: ${widthTextValue}; width: ${widthTextValue}; max-width: ${widthTextValue};</c:set>
</c:if>
<c:if test="${empty styleTextValue}">
	<c:set var="styleTextValue">width: 100%;</c:set>
</c:if>

<c:choose>
	<c:when test="${not empty id}">
		<c:set var="uiid" value="${id}"/>
	</c:when>
	<c:otherwise>
		<c:set var="uiid" value="${u:uiid()}"/>
	</c:otherwise>
</c:choose>

<div class="btn-white combo ${styleClass}" id="${uiid}" style="${style}">
	<input type="hidden" name="${hiddenName}" value="${value}"/>

	<c:if test="${not empty prefixText}">
		<div class="text-pref">${prefixText}</div>
	</c:if>

	<%-- you can set the width of the whole element by setting the width of below block --%>
	<div class="text-value" style="${styleTextValue}"></div>
	<div class="icon ti-angle-down"></div>

	<ul class="drop" style="display: none;">
		<c:if test="${showFilter}">
			<li class="filter">
				<input type="text" style="width: 100%;" placeholder="${l.l('Filter')}" onkeyup="$$.ui.comboSingleFilter(this)"/>
			</li>
		</c:if>

		${valuesHtml}

		<c:choose>
			<c:when test="${empty available}">
				<c:forEach var="item" items="${list}">
					<li value="${item.id}">${item.title}</li>
				</c:forEach>
			</c:when>
			<c:otherwise>
				<c:forEach var="availableId" items="${available}">
					<c:set var="item" value="${map[availableId]}"/>
					<c:if test="${not empty item }">
						<li value="${item.id}">${item.title}</li>
					</c:if>
				</c:forEach>
			</c:otherwise>
		</c:choose>

		<script style="display: none;">
			$(function () {
				let onSelect = undefined;

				const $comboDiv = $('#${uiid}');
				const $hidden = $comboDiv.find('input[type=hidden]');

				<c:if test="${not empty onSelect}">
					onSelect = function (item) {
						${onSelect}
					};
				</c:if>

				$$.ui.comboSingleInit($('#${uiid}'), onSelect);

				<c:if test="${not empty disable}">
					$comboDiv.unbind('click');
				</c:if>
			})
		</script>
	</ul>
</div>