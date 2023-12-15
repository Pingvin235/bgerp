<%@ tag body-content="empty" pageEncoding="UTF-8" description="Drop down list with multiselect"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
You can use the following methods to set values:

1)
list - List<IdTitle> of elements
map - Map<Integer, IdTitle> of elements
available - List<Integer> of allowed values

2)
valuesHtml - HTML-text with values as li elements

You can set width either via 'style' or 'styleTextValue' or 'widthTextValue'
Use styleTextValue / widthTextValue in situations when you expect a long value inside your list.
--%>

<%@ attribute name="id" description="id of outer DIV, auto generated if not explicitly specified"%>
<%@ attribute name="paramName" description="input name in а checkbox"%>
<%@ attribute name="prefixText" description="text prefix"%>
<%@ attribute name="values" type="java.util.Collection" description="current values"%>
<%@ attribute name="onChange" description="The action to be triggered on onchange"%>
<%@ attribute name="showFilter" description="Enable filter"%>
<%@ attribute name="style" description="outer DIV style"%>
<%@ attribute name="styleClass" description="outer DIV class"%>
<%@ attribute name="styleTextValue" description="current value's DIV style"%>
<%@ attribute name="widthTextValue" description="current value's block width"%>
<%@ attribute name="valuesHtml" description="HTML-text with values as li elements"%>

<%@ attribute name="list" type="java.util.Collection" description="List&lt;IdTitle&gt; of elements, refer to description inside tag"%>
<%@ attribute name="map" type="java.util.Map" description="Map&lt;Integer, IdTitle&gt; of elements, refer to description inside tag"%>
<%@ attribute name="available" type="java.util.Collection" description="List&lt;Integer&gt; of allowed values, refer to description inside tag"%>

<c:if test="${not empty widthTextValue}">
	<c:set var="styleTextValue">width: ${widthTextValue}; max-width: ${widthTextValue};</c:set>
</c:if>
<c:if test="${empty styleTextValue}">
	<c:set var="styleTextValue">width: 100%;</c:set>
</c:if>

<c:set var="showFilter" value="${u.parseBoolean(showFilter)}"/>

<c:choose>
	<c:when test="${not empty id}">
		<c:set var="uiid" value="${id}"/>
	</c:when>
	<c:otherwise>
		<c:set var="uiid" value="${u:uiid()}"/>
	</c:otherwise>
</c:choose>

<div class="btn-white combo ${styleClass}" id="${uiid}" style="${style}">
	<c:if test="${not empty prefixText}">
		<div class="text-pref">${prefixText}</div>
	</c:if>

	<%-- you can set the width of the whole element by setting the width of below block --%>
	<div class="text-value" style="${styleTextValue}"></div>
	<div class="icon"><i class="ti-close"></i></div>
	<ul class="drop" style="display: none;">
		<c:if test="${showFilter}">
			<li class="in-table-cell">
				<c:set var="filterCode">
					var mask = $(this).val().toLowerCase();
					$(this).closest('ul').find('li:gt(0)').each( function()
					{
						var content = $(this).text().toLowerCase();
						$(this).toggle( content.indexOf( mask ) >= 0 );
					});
				</c:set>
				<div style="width: 100%;"><input type="text" style="width: 100%;" placeholder="Filter" onkeyup="${filterCode}"/></div>
				<div class="pl05"><div class="btn-white btn-icon" onclick='$$.ui.combocheck.uncheck(this)' title="${l.l('Select all / remove selection')}"><i class="ti-check"></i></div></div>
			</li>
		</c:if>
		<data><%--
		--%>${valuesHtml}<%--
		--%><c:choose><%--
			--%><c:when test="${empty available}"><%--
				--%><c:forEach var="item" items="${list}"><%--
					--%><li><%--
						--%><input type="checkbox" name="${paramName}" value="${item.id}"  ${u:checkedFromCollection( values, item.id )}/> <%--
						--%><span>${item.title}</span><%--
					--%></li><%--
				--%></c:forEach><%--
			--%></c:when><%--
			--%><c:otherwise><%--
				--%><c:choose><%--
					--%><c:when test="${map ne null}"><%--
						--%><c:forEach var="availableId" items="${available}"><%--
							--%><c:set var="item" value="${map[availableId]}"/><%--
							--%><c:if test="${not empty item}"><%--
								--%><li><%--
									--%><input type="checkbox" name="${paramName}" value="${item.id}"  ${u:checkedFromCollection( values, item.id )}/> <%--
									--%><span>${item.title}</span><%--
								--%></li><%--
							--%></c:if><%--
						--%></c:forEach><%--
					--%></c:when><%--
					--%><c:otherwise><%--
						--%><c:forEach var="availableId" items="${available}"><%--
							--%><c:forEach var="item" items="${list}"><%--
								--%><c:if test="${availableId eq item.id}"><%--
									--%><li><%--
										--%><input type="checkbox" name="${paramName}" value="${item.id}"  ${u:checkedFromCollection( values, item.id )}/> <%--
										--%><span>${item.title}</span><%--
									--%></li><%--
								--%></c:if><%--
							--%></c:forEach><%--
						--%></c:forEach><%--
					--%></c:otherwise><%--
				--%></c:choose><%--
			--%></c:otherwise>
			</c:choose>
		</data>
	</ul>

	<script>
		$(function () {
			const $comboDiv = $('#${uiid}');

			let onChange = undefined;
			<c:if test="${not empty onChange}">
				onChange = function () {
					${onChange}
				}
			</c:if>

			$$.ui.combocheck.init($comboDiv, onChange);
		})
	</script>
</div>