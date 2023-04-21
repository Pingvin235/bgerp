<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
	<c:param name="action" value="getScript"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<button type="button" class="btn-green mb1" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())">+</button>

<div id="${uiid}">
	<table class="data" style="width:100%;">
		<tr class="header">
			<td width="30"></td>
			<td>Скрипт</td>
			<td>Период</td>
			<td width="100%">Комментарий</td>
		</tr>

		<c:forEach var="script" items="${form.response.data.scriptList}" varStatus="status">
			<tr>
				<c:url var="eUrl" value="${url}">
					<c:param name="scriptId" value="${script.id}"/>
				</c:url>
				<c:set var="editCommand" value="$$.ajax.load('${eUrl}', $('#${uiid}').parent())"/>

				<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/contract.do">
					<c:param name="action" value="deleteScript"/>
					<c:param name="contractId" value="${form.param.conractId}"/>
					<c:param name="billingId" value="${form.param.billingId}"/>
					<c:param name="scriptId" value="${script.id}"/>
				</c:url>
				<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent())"/>
				<td nowrap="nowrap">
					<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				</td>
				<td nowrap="nowrap" >${script.getTitle()}</td>
				<td align="center" nowrap="nowrap">${script.getPeriod()}</td>
				<td nowrap="nowrap" width="100%">${script.getComment()}</td>
			</tr>
		</c:forEach>
	</table>
</div>

<h2>Логи выполнения</h2>
<div>
	<%@ include file="/WEB-INF/jspf/user/plugin/bgbilling/contract/script/script_log.jsp"%>
</div>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function()
	{
		var scripts = "";

		<c:forEach var="item" items="${contractInfo.scriptList}" varStatus="status">
			scripts += "<div>${item.title}";
			<c:if test="${not status.last}">
				scripts += ", ";
			</c:if>
			scripts += "</div> ";
		</c:forEach>

		$('#${contractTreeId} #treeTable div#scripts').html( scripts );
	})
</script>