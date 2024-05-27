<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>
<c:url var="addUrl" value="${url}">
	<c:param name="method" value="getScript"/>
	<c:param name="scriptId" value="-1"/>
</c:url>
<ui:button type="add" styleClass="btn-green mb1" onclick="$$.ajax.load('${addUrl}', $('#${uiid}').parent())"/>

<div id="${uiid}">
	<table class="data hl">
		<tr>
			<td width="30"></td>
			<td>Скрипт</td>
			<td>Период</td>
			<td width="100%">Комментарий</td>
		</tr>

		<c:forEach var="script" items="${frd.scriptList}" varStatus="status">
			<tr>
				<td class="nowrap">
					<c:url var="editUrl" value="${url}">
						<c:param name="method" value="getScript"/>
						<c:param name="scriptId" value="${script.id}"/>
					</c:url>
					<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $('#${uiid}').parent())"/>

					<c:url var="delUrl" value="${url}">
						<c:param name="method" value="deleteScript"/>
						<c:param name="scriptId" value="${script.id}"/>
					</c:url>
					<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${delUrl}').done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))"/>
				</td>
				<td class="nowrap" >${script.script}</td>
				<td align="center" class="nowrap">${script.period}</td>
				<td class="nowrap" width="100%">${script.comment}</td>
			</tr>
		</c:forEach>
	</table>
</div>

<h2>Логи выполнения</h2>
<div>
	<%@ include file="/WEB-INF/jspf/user/plugin/bgbilling/contract/script/script_log.jsp"%>
</div>

<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree" />
<script>
	$(function () {
		let scripts = "";

		<c:forEach var="item" items="${contractInfo.scriptList}" varStatus="status">
			scripts += "<div>${item.title}";
				<c:if test="${not status.last}">
					scripts += ", ";
				</c:if>
				scripts += "</div> ";
		</c:forEach>

		$('#${contractTreeId} #treeTable div#scripts').html(scripts);
	})
</script>