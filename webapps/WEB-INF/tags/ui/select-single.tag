<%@ tag body-content="empty" pageEncoding="UTF-8" description="Выпадающий список с возможностью выбора одного значения и поиском в редактируемом поле"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Значения устанавиваются атрибутами:

list - List<IdTitle> элементов
map - Map<Integer, IdTitle> элементов
availableIdList - List id допустимых значений
availableIdSet - Set id допустимых значений

Если указано availableIdList, то выборка происходит по нему с получением значений из map.
Иначе - по list и в его порядке с фильтром значений по availableIdSet.
--%>

<%@ attribute name="id" description="id внешнего DIVа, если не указан - генерируется"%>
<%@ attribute name="hiddenName" description="имя hidden параметра"%>
<%@ attribute name="value" description="текущее значение hidden параметра"%>
<%@ attribute name="style" description="стиль внешнего div а"%>
<%@ attribute name="styleClass" description="класс внешнего div а"%>
<%@ attribute name="placeholder" description="placeholder внутреннего input а"%>
<%@ attribute name="inputAttrs" description="любые атрибуты input а text"%>
<%@ attribute name="onSelect" description="что выполнять по выбору значения"%>
<%@ attribute name="additionalSourceFilter" description="кусок JS кода для дополнительной фильтрации source при выводе"%>

<%@ attribute name="showId" type="java.lang.Boolean"  description="отображать код значения"%>
<%@ attribute name="showType" type="java.lang.Boolean" description="отображать тип"%>
<%@ attribute name="showComment" type="java.lang.Boolean" description="отображать комментарий"%>

<%@ attribute name="list" type="java.util.Collection" description="List&lt;IdTitle&gt; элементов, см. описание в теге"%>
<%@ attribute name="map" type="java.util.Map" description="Map&lt;Integer, IdTitle&gt; элементов, см. описание в теге"%>
<%@ attribute name="availableIdList" type="java.util.List" description="List допустимых значений, см. описание в теге"%>
<%@ attribute name="availableIdSet" type="java.util.Set" description="Set допустимых значений, см. описание в теге"%>


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
						<%-- удалённые не отображаются и недоступные --%>
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
							<%-- удалённые не отображаются и недоступные --%>
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
					<%-- передаётся при инклуде из select_mult.jsp --%>
					${additionalSourceFilter}
					response( filteredSource );
				},
				select: function( event, ui )
				{
					$hidden.val( ui.item.id );
					$input.val( ui.item.value );
					${onSelect}
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