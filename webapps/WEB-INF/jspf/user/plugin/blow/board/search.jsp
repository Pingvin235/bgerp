<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="combo" style="display: block;">
	<ul class="drop" style="display: none; margin-top: 0px;" id="${u:uiid()}">
		<c:forEach var="item" items="${frd.list}">
			<li><ui:process-link id="${item.processId}"/> [${item.hits.size()}] ${item.processDescription}</li>
		</c:forEach>
	</ul>
</div>