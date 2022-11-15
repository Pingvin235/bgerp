<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}callListEditor">
	<table class="mb1">
		<tr>
			<td width="100%">
				<div class="tableIndent">
					<c:url var="createUrl" value="empty.do">
						<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/crm/call/call_editor_tab.jsp" />
						<c:param name="billingId" value="${form.param.billingId }" />
						<c:param name="contractId" value="${form.param.contractId }" />
						<c:param name="uiid" value="${uiid }" />
					</c:url>
					<button type="button" class="btn-green" onClick="$$.ajax.load('${createUrl}', $('#${uiid}callListEditor') )">+</button>
				</div>
			</td>
			<td>
				<form action="plugin/bgbilling/proto/billingCrm.do">
					<input type="hidden" name="action" value="callList"/>
					<input type="hidden" name="billingId" value="${form.param.billingId }"/>
					<input type="hidden" name="contractId" value="${form.param.contractId }"/>

					<c:set var="nextCommand" value="; openUrlToParent( formUrl( this.form ), $('#${uiid}callListEditor') )"/>
					<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
				</form>
			</td>
		</tr>
	</table>
	<table class="data" width="100%">
		<tr>
			<td>${l.l('Код')}</td>
			<td>${l.l('Договор')}</td>
			<td>${l.l('Type')}</td>
			<td>${l.l('Время')}</td>
			<td>${l.l('Принят')}</td>
			<td>${l.l('Проблема')}</td>
		</tr>

		<c:forEach var="call" items="${form.response.data.list}">
			<tr>
				<td align="center">${ call.getId() }</td>
				<td nowrap="nowrap" align="center">${ call.getContract() }</td>
				<td>${ call.getType() }</td>
				<td align="center">${ call.getTime() }</td>
				<td nowrap="nowrap">${ call.getUser() }</td>
				<td>${ call.getProblem() }</td>
			</tr>
		</c:forEach>
	</table>
</div>
