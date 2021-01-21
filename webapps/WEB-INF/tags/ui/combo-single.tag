<%@ tag body-content="empty" pageEncoding="UTF-8" description="Выпадающий список с возможностью выбора одного значения"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Значения можно устанавливать двумя способами:

1)
list - List<IdTitle> элементов
map - Map<Integer, IdTitle> элементов
available - List<Integer> допустимых значений
TODO: может по аналогии сделать с select_single availableIdList?

Если указано available, то выборка происходит по нему с получением значений из map.
Иначе - по list и в его порядке

2)
valuesHtml - HTML текст с li элементами - значениями

Установку ширины следует выполнять с помощью style, либо styleTextValue либо  widthTextValue.
styleTextValue / widthTextValue следует использовать, когда в список может попасть длинное значение.
--%>

<%@ attribute name="id" description="id внешнего DIVа, если не указан - генерируется"%>
<%@ attribute name="hiddenName" description="имя hidden параметра"%>
<%@ attribute name="prefixText" description="текстовый префикс"%>
<%@ attribute name="value" description="текущее значение hidden параметра"%>
<%@ attribute name="style" description="стиль внешнего DIVа"%>
<%@ attribute name="styleClass" description="класс внешнего DIVа"%>
<%@ attribute name="styleTextValue" description="стиль DIVа с отображаением значения"%>
<%@ attribute name="widthTextValue" description="ширина блока с текущим значением"%>
<%@ attribute name="onSelect" description="JS, что выполнять по выбору значения"%>
<%@ attribute name="disable" description="блокировка изменений (TODO: сделать другой цвет)"%>
<%@ attribute name="showFilter" type="java.lang.Boolean" description="отображать фильтр"%>
<%@ attribute name="valuesHtml" description="HTML текст с li элементами - значениями, см. описание в теге"%>

<%@ attribute name="list" type="java.util.Collection" description="List значений, см. описание в теге"%>
<%@ attribute name="map" type="java.util.Map" description="Map значений, см. описание в теге"%>
<%@ attribute name="available" type="java.util.Collection" description="Set разрешённых id, см. описание в теге"%>

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

	<%-- ширину всего элемента можно задавать только шириной этого блока --%>
	<div class="text-value" style="${styleTextValue}"></div>
	<div class="icon"><img src="/images/arrow-down.png"/></div>

	<ul class="drop" style="display: none;">
		<c:if test="${showFilter}">
			<li class="filter">
				<c:set var="filterCode">
					var mask = $(this).val().toLowerCase();
					$(this.parentNode.parentNode).find('li:gt(0)').each( function()
					{
						var content = $(this).text().toLowerCase();
						$(this).toggle( content.indexOf( mask ) >= 0 );
					});
				</c:set>
				<input type="text" style="width: 100%;" placeholder="${l.l('Фильтр')}" onkeyup="${filterCode}"/>
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
				var onSelect = undefined;

				var $comboDiv = $('#${uiid}');

				<c:if test="${not empty onSelect}">
					onSelect = function (item) {
						var $hidden = $comboDiv.find( 'input[type=hidden]' );
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