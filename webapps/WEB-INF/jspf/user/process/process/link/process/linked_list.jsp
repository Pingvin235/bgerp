<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="linkAddUiid" value="${ctxUser.checkPerm('org.bgerp.action.ProcessLinkProcessAction:linkedProcessAdd') ? u:uiid() : ''}"/>
<c:set var="list" value="${form.response.data.list}"/>
<c:set var="mode" value="linked"/>

<c:if test="${not empty linkAddUiid or not empty list}">
	<c:set var="linkFormUiid" value="${u:uiid()}"/>

	<%@ include file="pagination.jsp"%>

	<c:if test="${not empty linkAddUiid}">
		<div id="${linkAddUiid}" class="mb1" style="display: none;">
			<%@ include file="add_existing.jsp"%>

			<%--
				preconfigured queue selection
			--%>

			<ui:button type="close" styleClass="btn-white mt1" onclick="$('#${linkAddUiid}').toggle(); $('#${linkFormUiid}').toggle();"/>
		</div>
	</c:if>

	<c:set var="mode" value="linked"/>
	<%@ include file="table.jsp"%>
</c:if>