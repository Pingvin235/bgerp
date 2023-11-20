<%@ tag body-content="empty" pageEncoding="UTF-8" description="Drop down list with multiselect"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
	Button for choosing action permissions in drop-down tree.
--%>

<%@ attribute name="permTrees" required="true" type="java.util.Collection" description="List with root nodes of permission trees"%>
<%@ attribute name="id" description="CSS ID for external div, if not defined than generated"%>
<%@ attribute name="prefixText" description="text prefix"%>
<%@ attribute name="values" type="java.util.Collection" description="current values"%>
<%@ attribute name="style" description="CSS style for external div"%>
<%@ attribute name="styleClass" description="CSS classes for external div"%>
<%@ attribute name="styleTextValue" description="CSS style of div with text value"%>
<%@ attribute name="widthTextValue" description="width of div with text value"%>


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
	<div class="text-value" style="${styleTextValue}">[0]</div>
	<div class="icon"><i class="ti-close"></i></div>

	<c:set var="permTreeId" value="${u:uiid()}"/>

	<div class="drop p05" style="width:100%; display: none;">
		<ul id="${permTreeId}">
			<c:forEach var="root" items="${permTrees}">
				<ui:combo-perm-tree-check-node node="${root}" values="${values}"/>
			</c:forEach>
		</ul>
	</div>

	<script>
		$$.ui.comboPermTreeCheckInit($("#${uiid}"));
		$("#${permTreeId}").Tree();
	</script>
</div>