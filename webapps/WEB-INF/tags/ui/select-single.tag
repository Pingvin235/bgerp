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
<%@ attribute name="additionalSourceFilter" description="JS code to be used for additional source filtering on output"%>

<%@ attribute name="showId" type="java.lang.Boolean"  description="show Id (boolean)"%>
<%@ attribute name="showType" type="java.lang.Boolean" description="show type (boolean)"%>
<%@ attribute name="showComment" type="java.lang.Boolean" description="show comments (boolean)"%>

<%@ attribute name="list" type="java.util.Collection" description="List&lt;IdTitle&gt; of elements, refer to description inside tag"%>
<%@ attribute name="map" type="java.util.Map" description="Map&lt;Integer, IdTitle&gt; of elements, refer to description inside tag"%>
<%@ attribute name="availableIdList" type="java.util.List" description="List of allowed values, refer to description inside tag"%>
<%@ attribute name="availableIdSet" type="java.util.Set" description="Set of allowed values, refer to description inside tag"%>

<c:choose>
	<c:when test="${not empty id}">
		<c:set var="uiid" value="${id}"/>
	</c:when>
	<c:otherwise>
		<c:set var="uiid" value="${u:uiid()}"/>
	</c:otherwise>
</c:choose>

<div class="select ${styleClass}" style="${style}" id="${uiid}">
	<input type="hidden" name="${hiddenName}" value="${value}"/>
	<input type="text" name="data" ${inputAttrs} style="width: 100%;" placeholder="${placeholder}"/>
	<span class="icon"><i class="ti-angle-down"></i></span>

	<script>
		$(function()
		{
			var $selectDiv = $('#${uiid}');

			var $input = $selectDiv.find( "input[type='text']" );
			var $hidden = $selectDiv.find( "input[type='hidden']" );

			$selectDiv.find( '.icon' ).click( function()
			{
				$input.autocomplete( "search", "" );

				$(document).one( "click", function() {
					$selectDiv.find( "ul" ).hide();
				});

				return false;
			});

			var $source = [];
			var title = "";

			var itemTitle = "";

			<c:choose>
				<c:when test="${empty availableIdList}">
					<c:forEach var="item" items="${list}">
						<%-- do not show deleted and unavailable --%>
						<c:if test="${not fn:startsWith(item.title, '@') and (empty availableIdSet or availableIdSet.contains(item.id))}">
							itemTitle = "${u:quotEscape( item.title )}";

							<c:if test="${showId}">itemTitle += " (${item.id})";</c:if>
							<c:if test="${showType}">itemTitle += " (${item.type})";</c:if>
							<c:if test="${showComment and not empty item.comment}">itemTitle += " (${u:quotEscape( item.comment )})";</c:if>

							$source.push({ id : "${item.id}", value: itemTitle });
						</c:if>
						<c:if test="${value eq item.id}">
							title = "${u:quotEscape( item.title )}";
						</c:if>
					</c:forEach>
				</c:when>
				<c:otherwise>
					<c:forEach var="availableId" items="${availableIdList}">
						<c:set var="item" value="${map[availableId]}"/>
						<c:if test="${not empty item}">
							<%-- do not show deleted and unavailable --%>
							<c:if test="${not fn:startsWith(item.title, '@') and (empty availableIdSet or availableIdSet.contains(item.id))}">
								itemTitle = "${item.title}";

								<c:if test="${showId}">itemTitle += " (${item.id})";</c:if>
								<c:if test="${showComment and not empty item.comment}">itemTitle += " (${u:quotEscape( item.comment )})";</c:if>

								$source.push({ id : "${item.id}", value: itemTitle });
							</c:if>
						</c:if>
						<c:if test="${value eq item.id}">
							title = "${u:quotEscape( item.title )}";
						</c:if>
					</c:forEach>
				</c:otherwise>
			</c:choose>

			$input.val( title );

			$input.autocomplete(
			{
				minLength: 0,
				source: function( request, response )
				{
					var filteredSource = $.ui.autocomplete.filter( $source, request.term );
					<%-- gets passed if included from select_mult.jsp --%>
					${additionalSourceFilter}
					response( filteredSource );
				},
				select: function( event, ui )
				{
					$hidden.val(ui.item.id);
					$input.val(ui.item.value);

					$hidden[0].onSelect = function () {
						${onSelect}
					}
					// to make this equals hidden input
					$hidden[0].onSelect();
				},
				appendTo: '#${uiid}',
				open: function()
				{
					// удаление вычисленных ширин и позиций
					$input.autocomplete( "widget" ).css( "width", "" ).css( "top", "" ).css( "left", "" );
				}
			});

			$input.keyup( function()
			{
				if( !this.value )
				{
					$hidden.val( "" );
					<%-- Вроде не нужно ${onSelect}--%>
				}
			});

			$selectDiv.find( "ul" ).removeClass( "ui-autocomplete ui-front" );
		})
	</script>
</div>