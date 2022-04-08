<%@ tag body-content="empty" pageEncoding="UTF-8" description="Drop down list with multiselect"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Значения можно устанавливать двумя способами:

1)
list - List<IdTitle> элементов
map - Map<Integer, IdTitle> элементов
available - List<Integer> допустимых значений

2)
valuesHtml - HTML текст с li элементами - значениями

Установку ширины следует выполнять с помощью style, либо styleTextValue либо  widthTextValue.
styleTextValue / widthTextValue следует использовать, когда в список может попасть длинное значение.
--%>

<%@ attribute name="id" description="id внешнего DIVа, если не указан - генерируется"%>
<%@ attribute name="paramName" description="имя input а checkbox"%>
<%@ attribute name="prefixText" description="текстовый префикс"%>
<%@ attribute name="values" type="java.util.Collection" description="текущие значения"%>
<%@ attribute name="onChange" description="что выполнять по изменению значений"%>
<%@ attribute name="showFilter" type="java.lang.Boolean"  description="отображать фильтр"%>
<%@ attribute name="style" description="стиль внешнего DIVа"%>
<%@ attribute name="styleClass" description="доп. классы внешнего DIVа"%>
<%@ attribute name="styleTextValue" description="стиль DIVа с отображаением значения"%>
<%@ attribute name="widthTextValue" description="ширина блока с текущим значением"%>
<%@ attribute name="valuesHtml" description="HTML текст с li элементами - значениями"%>

<%@ attribute name="list" type="java.util.Collection" description="List&lt;IdTitle&gt; элементов, см. описание в теге"%>
<%@ attribute name="map" type="java.util.Map" description="Map&lt;Integer, IdTitle&gt; элементов, см. описание в теге"%>
<%@ attribute name="available" type="java.util.Collection" description="List&lt;Integer&gt; допустимых значений, см. описание в теге"%>

<c:if test="${not empty widthTextValue}">
	<c:set var="styleTextValue">width: ${widthTextValue}; max-width: ${widthTextValue};</c:set>
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
	<c:if test="${not empty prefixText}">
		<div class="text-pref">${prefixText}</div>
	</c:if>

	<%-- the whole width is defined by this one --%>
	<div class="text-value" style="${styleTextValue}"></div>
	<div class="icon"><i class="ti-close"></i></div>
	<ul class="drop" style="display: none;">
		<c:if test="${showFilter}">
			<li class="in-table-cell">
				<c:set var="filterCode">
					var mask = $(this).val().toLowerCase();
					$(this).closest('ul').find('li:gt(0)').each(function () {
						var content = $(this).text().toLowerCase();
						$(this).toggle( content.indexOf( mask ) >= 0 );
					});
				</c:set>
				<div style="width: 100%;"><input type="text" style="width: 100%;" placeholder="Фильтр" onkeyup="${filterCode}"/></div>
				<div class="pl05"><div class="btn-white btn-icon" onclick='uiComboCheckUncheck(this)' title="${l.l('Выделить все / снять выделение')}"><i class="ti-check"></i></div></div>
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
		$(function()
		{
			var $comboDiv = $('#${uiid}');
			var $drop = $comboDiv.find('ul.drop');

			var updateCurrentTitle = function()
			{
				var checkedCount = 0;
				var titles = "";

				$comboDiv.find( "ul.drop li input[type=checkbox]" ).each( function()
				{
					if( this.checked )
					{
						checkedCount++;
						var title = $(this).next().text();
						if( titles.length > 0 )
						{
							titles += ", ";
						}
						titles += title;
					}
				});

				$comboDiv.find( '.text-value' ).text( "[" + checkedCount + "] " + titles );

				${onChange}
			};

 			$comboDiv.find( "ul.drop" ).on( "click", "li input", function( event )
			{
				updateCurrentTitle();
				event.stopPropagation();
			});

			$comboDiv.find( "ul.drop" ).on( "click", "li", function()
			{
				var input = $(this).find( "input" )[0];

				input.checked = !(input.checked);

				updateCurrentTitle();

				return false;
			})

			$$.ui.dropOnClick($comboDiv, $drop);

			$comboDiv.find( "div.icon" ).click( function( event )
			{
				$comboDiv.find( "ul.drop li input" ).each( function()
				{
					this.checked = false;
				});
				updateCurrentTitle();

				event.stopPropagation();
			});

			updateCurrentTitle();
		})
	</script>
</div>