<%@ tag body-content="empty" pageEncoding="UTF-8" description="List item for select-mult drop-down list"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="item" type="org.bgerp.model.base.iface.IdTitle" required="true" description="item to be shown in the li"%>
<%@ attribute name="name" required="true" description="hidden input's name"%>
<%@ attribute name="showId" type="java.lang.Boolean" description="show item's ID"%>
<%@ attribute name="showComment" type="java.lang.Boolean" description="show item's comment"%>
<%@ attribute name="upDownIcons" description="optional HTML block with up and down icons"%>
<%@ attribute name="onChange" description="JS call when values were deleted"%>

<c:set var="title">${item.title}
	<c:if test="${showId}">&nbsp;(${item.id})</c:if>
	<c:if test="${showComment and not empty item.comment}">&nbsp;(${item.comment})</c:if>
</c:set>
<li title="${title}">
	<span class="delete ti-close" onclick="$$.ui.select.mult.liDel(this); ${onChange}"></span>
	<span class="title">${title}</span>
	${upDownIcons}
	<input type="hidden" name="${name}" value="${item.id}"/>
</li>