<%@ tag body-content="empty" pageEncoding="UTF-8" description="Выпадающий список с возможностью выбора нескольких значений в т.ч. с указанием порядка"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- 
Значения устанавиваются атрибутами: 

list - List<IdTitle> элементов 
map - Map<Integer, IdTitle> элементов
availableIdList - List id допустимых значений
availableIdSet - Set id допустимых значений

Для списка предлагаемых значений:
Если указано availableIdList, то выборка происходит по нему с получением значений из map.
Иначе - по list и в его порядке с опциональным фильтром значений по availableIdSet.
--%>

<%@ attribute name="id" description="id внешнего DIVа, если не указан - генерируется"%>
<%@ attribute name="hiddenName" description="имя hidden параметров"%>
<%@ attribute name="values" type="java.util.Collection" description="текущие значения"%>
<%@ attribute name="style" description="стиль внешнего div а"%>
<%@ attribute name="styleClass" description="класс внешнего div а"%>
<%@ attribute name="placeholder" description="placeholder внутреннего input а"%>
<%@ attribute name="onSelect" description="что выполнять по выбору значения"%>

<%@ attribute name="showId" type="java.lang.Boolean" description="отображать код значения"%>
<%@ attribute name="showComment" type="java.lang.Boolean" description="отображать комментарий"%>
<%@ attribute name="moveOn" type="java.lang.Boolean" description="отображать изменение порядка"%>
<%@ attribute name="fakeHide" type="java.lang.Boolean" description="скрывать элементы со свойством fake из выбранных"%>

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

<%-- TODO: В дальнейшем функцию перенести в JS файл, пока сдесь для предотвращения конфликтов. --%>
<script>
	function liUp( el )
	{
		var currentLi = el.parentNode;
		var $prev = $(currentLi).prev();	
		if( $prev.length > 0 )
		{
			$(currentLi).insertBefore( $prev );
		}
	}
	
	function liDown( el )
	{
		var currentLi = el.parentNode;
		var $next = $(currentLi).next();	
		if( $next.length > 0 )
		{
			$(currentLi).insertAfter( $next );
		}
	}
</script>

<c:set var="upDownIcons">
	<span class='up' onClick='liUp(this);'></span><span class='down' onClick='liDown(this);'></span>
</c:set>	

<div class="select-mult ${styleClass}" style="${style}" id="${uiid}">
	<div style="display:table; width: 100%;">
		<div style="display: table-cell; width: 100%;">
			<u:sc>
				<c:set var="additionalSourceFilter">
					var selectedValues = {};
					$('#${uiid} ul.drop-list li input').each(function()
					{
						selectedValues[$(this).val()] = 1;
					});
					
					var filteredSourceWithoutSelected = [];
					for( var i = 0; i < filteredSource.length; i++ )
					{
						if( !selectedValues[filteredSource[i].id] )
						{
							filteredSourceWithoutSelected.push( filteredSource[i] );
						}
					}
					
					filteredSource = filteredSourceWithoutSelected;
				</c:set>
				<c:set var="onSelect">
					var id = $hidden.val();
					if( !id )
					{
						alert( 'Выберите значение' );
						return;
					}
					
					var title = $input.val();
					
					$('#${uiid} ul.drop-list').append( 
						sprintf( '<li>\
						            <span class=\'delete\' onclick=\'$(this.parentNode).remove();\'></span>\
						            ${fn:replace( upDownIcons, "'", "\\'")}\
						            <span class=\'title\'>%s</span>\
								    <input type=\'hidden\' name=\'${hiddenName}\' value=\'%s\'/>\
							       </li>', title, id ) );
					
					$input.val( '' );
					$hidden.val( '' );
					
					// иначе опять ставит в $input текст
					return false;					
				</c:set>
				
				<%-- иначе при явном указании id для select_mult такой же попадёт и в select_single, выпадающий ul добавится не туда --%>
				<c:remove var="id"/>
				<ui:select-single hiddenName="${uiid}-addingValue" style="width: 100%;"
					showId="${showId}" showType="${showType}" showComment="${showComment}"
					onSelect="${onSelect}" additionalSourceFilter="${additionalSourceFilter}"
					list="${list}" map="${map}" availableIdList="${availableIdList}" availableIdSet="${availableIdSet}"/>
			</u:sc>
		</div>
	</div>
	
	<div class="layout-height-rest" style="overflow-y: auto;">
		<c:choose>
			<c:when test="${moveOn}">
				<ul class="drop-list move-on">
					<c:forEach var="id" items="${values}">
						<c:set var="item" value="${map[id]}"/>
						<c:if test="${not empty item}">
						    <%@ include file="/WEB-INF/jspf/tag_select_mult_li.jsp"%>
						</c:if>
					</c:forEach>
				</ul>
			</c:when>
			<c:otherwise>
				<ul class="drop-list">
					<c:choose>
						<c:when test="${empty availableIdList}">
							<c:forEach var="item" items="${list}">
								<c:if test="${values.contains( item.id ) and (not fakeHide or empty item.fake)}">
									<%@ include file="/WEB-INF/jspf/tag_select_mult_li.jsp"%>
								</c:if>
							</c:forEach>
						</c:when>
						<c:otherwise>
							<c:forEach var="availableId" items="${availableIdList}">			
								<c:set var="item" value="${map[availableId]}"/>
								<c:if test="${values.contains( item.id ) and (not fakeHide or empty item.fake)}">
									<%@ include file="/WEB-INF/jspf/tag_select_mult_li.jsp"%>	
								</c:if>
							</c:forEach>
						</c:otherwise>
					</c:choose>
				</ul>
			</c:otherwise>
		</c:choose>
	</div>	
</div>	