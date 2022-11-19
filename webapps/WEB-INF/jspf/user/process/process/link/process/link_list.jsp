<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="linkAddUiid" value="${ctxUser.checkPerm('org.bgerp.action.ProcessLinkProcessAction:linkProcessAdd') or ctxUser.checkPerm('org.bgerp.action.ProcessLinkProcessAction:linkProcessCreate') ? u:uiid() : ''}"/>
<c:set var="list" value="${form.response.data.list}"/>
<c:set var="mode" value="link"/>

<c:if test="${not empty linkAddUiid or not empty list}">
	<c:set var="linkFormUiid" value="${u:uiid()}"/>

	<%@ include file="pagination.jsp"%>

	<c:if test="${not empty linkAddUiid}">
		<div id="${linkAddUiid}" class="mb1" style="display: none;">
			<%@ include file="add_existing.jsp"%>

			<%-- preconfigured process types, processCreateLink https://bgerp.org/doc/3.0/manual/kernel/process/#linked-process --%>
			<%@ include file="add_created.jsp"%>

			<ui:button type="close" styleClass="btn-white mt1" onclick="$('#${linkAddUiid}').toggle(); $('#${linkFormUiid}').toggle();"/>
		</div>
	</c:if>

	<%@ include file="table.jsp"%>
</c:if>