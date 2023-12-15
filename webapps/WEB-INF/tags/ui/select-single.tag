<%@ tag body-content="empty" pageEncoding="UTF-8" description="Dropdown list allowing a single value selection and search within an edit field"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
You can use the following methods to set values:

list - List<IdTitle> of elements
map - Map<Integer, IdTitle> of elements
availableIdList - List of allowed id values
availableIdSet - Set of allowed values for ids

If availableIdList is defined, then selection is done according to it with picking corresponding values from 'map'
Otherwise 'list' and its ordering are used, along with possibility of values filtering based on availableIdSet
--%>

<%@ attribute name="id" description="id of outer DIV, auto generated if not explicitly specified"%>
<%@ attribute name="hiddenName" description="hidden parameter name"%>
<%@ attribute name="value" description="hidden parameter's current value"%>
<%@ attribute name="style" description="outer DIV style"%>
<%@ attribute name="styleClass" description="outer DIV CSS class"%>
<%@ attribute name="placeholder" description="placeholder for an internal input field"%>
<%@ attribute name="inputAttrs" description="any input field attributes"%>
<%@ attribute name="onSelect" description="JS, action to be performed on value selection"%>

<%@ attribute name="showId" description="show Id"%>
<%@ attribute name="showComment" description="show comments"%>

<%@ attribute name="list" type="java.util.List" description="List&lt;IdTitle&gt; of elements, refer to description inside tag"%>
<%@ attribute name="availableIdSet" type="java.util.Set" description="Set of allowed values, refer to description inside tag"%>
<%@ attribute name="map" type="java.util.Map" description="Map&lt;Integer, IdTitle&gt; of elements, refer to description inside tag"%>
<%@ attribute name="availableIdList" type="java.util.List" description="List of allowed values, refer to description inside tag"%>

<c:choose>
	<c:when test="${not empty id}">
		<c:set var="uiid" value="${id}"/>
	</c:when>
	<c:otherwise>
		<c:set var="uiid" value="${u:uiid()}"/>
	</c:otherwise>
</c:choose>

<c:set var="showId" value="${u.parseBoolean(showId)}"/>
<c:set var="showComment" value="${u.parseBoolean(showComment)}"/>

<div class="select ${styleClass}" style="${style}" id="${uiid}">
	<input type="hidden" name="${hiddenName}" value="${value}"/>
	<input type="text" name="data" ${inputAttrs} style="width: 100%;" placeholder="${placeholder}"/>
	<span class="icon"><i class="ti-angle-down"></i></span>

	<script>
		$(function () {
			const source = ${ui.selectSingleSourceJson(list, availableIdSet, availableIdList, map, showId, showComment)};
			$$.ui.select.single.init('${uiid}', source, '${value}', (filteredSource) => { return filteredSource }, function ($hidden, $input) { ${onSelect} });
		})
	</script>
</div>