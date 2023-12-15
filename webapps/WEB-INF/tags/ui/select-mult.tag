<%@ tag body-content="empty" pageEncoding="UTF-8" description="Drop down list with multi-select, including ordered multi-select"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
You can use the following methods to set values:

list - List<IdTitle> of elements
map - Map<Integer, IdTitle> of elements
availableIdList - List id of allowed values
availableIdSet - Set of allowed values for ids

If availableIdList is defined, then selection is done according to it with picking corresponding values from 'map'
Otherwise 'list' and its ordering are used, along with possibility of values filtering based on availableIdSet
--%>

<%@ attribute name="id" description="id of outer DIV, auto generated if not explicitly specified"%>
<%@ attribute name="hiddenName" description="hidden parameter name"%>
<%@ attribute name="values" type="java.util.Collection" description="hidden parameter's current value"%>
<%@ attribute name="style" description="outer DIV style"%>
<%@ attribute name="styleClass" description="outer DIV class"%>
<%@ attribute name="placeholder" description="placeholder for an internal input field"%>
<%@ attribute name="onSelect" description="JS, action to be performed on value selection"%>

<%@ attribute name="showId" description="show Id"%>
<%@ attribute name="showComment" description="show comments"%>
<%@ attribute name="moveOn" description="show change in order"%>

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

<c:set var="showId" value="${u.parseBoolean(showId)}"/>
<c:set var="showComment" value="${u.parseBoolean(showComment)}"/>
<c:set var="moveOn" value="${u.parseBoolean(moveOn)}"/>

<c:set var="upDownIcons">
	<span class='up ti-angle-up' onClick='$$.ui.selectMult.liUp(this);'></span><span class='down ti-angle-down' onClick='$$.ui.selectMult.liDown(this);'></span>
</c:set>

<div class="select-mult ${styleClass}" style="${style}" id="${uiid}">
	<div style="display:table; width: 100%;">
		<div style="display: table-cell; width: 100%;">
			<u:sc>
				<c:set var="onSelect">
					var id = $hidden.val();
					if( !id )
					{
						alert( 'Please select a value' );
						return;
					}

					var title = $input.val();

					$('#${uiid} ul.drop-list').append(
						sprintf('<li>\
									<span class=\'delete ti-close\' onclick=\'$(this.parentNode).remove();\'></span>\
									${upDownIcons.replace("'", "\\'")}\
									<span class=\'title\'>%s</span>\
									<input type=\'hidden\' name=\'${hiddenName}\' value=\'%s\'/>\
								</li>', title, id ) );

					$input.val('');
					$hidden.val('');

					// otherwise text appears in $input again
					return false;
				</c:set>

				<%-- otherwise if id is explicitly defined for select_mult, then the same will also get into select_single, and a drop-down ul will be added into a wrong place --%>
				<c:remove var="id"/>
				<ui:select-single hiddenName="${uiid}-addingValue" style="width: 100%;"
					showId="${showId}" showComment="${showComment}"
					onSelect="${onSelect}"
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
								<c:if test="${values.contains(item.id)}">
									<%@ include file="/WEB-INF/jspf/tag_select_mult_li.jsp"%>
								</c:if>
							</c:forEach>
						</c:when>
						<c:otherwise>
							<c:forEach var="availableId" items="${availableIdList}">
								<c:set var="item" value="${map[availableId]}"/>
								<c:if test="${values.contains(item.id)}">
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