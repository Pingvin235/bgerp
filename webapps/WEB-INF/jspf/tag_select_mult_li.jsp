<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="title">${item.title}
	<c:if test="${showId}">&nbsp;(${item.id})</c:if>
	<c:if test="${showComment and not empty item.comment}">&nbsp;(${item.comment})</c:if>
</c:set>
<li title="${title}">
	<span class="delete ti-close" onclick="$(this.parentNode).remove();"></span>
	<span class="title">${title}</span>
	${upDownIcons}
	<input type="hidden" name="${hiddenName}" value="${item.id}"/>
</li>