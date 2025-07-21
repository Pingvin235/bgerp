<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data hl">
	<tr>
		<td width="30">&nbsp;</td>
		<td width="100%">Наименование</td>
	</tr>
	<c:forEach var="item" items="${list.first}">
		<tr>
			<td>
				<u:sc>
					<c:url var="url" value="${form.requestURI}">
						<c:param name="method" value="docTypeDelete"/>
						<c:param name="typeIds" value="${item.id}"/>
						<c:param name="moduleId" value="${form.param.moduleId}"/>
						<c:param name="contractId" value="${form.param.contractId}"/>
						<c:param name="billingId" value="${form.param.billingId}"/>
					</c:url>
					<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${url}').done(() => $$.ajax.load('${form.requestUrl}',$('#${uiid}').parent()))"/>
				</u:sc>
			</td>
			<td>${item.title}</td>
		</tr>
	</c:forEach>
</table>

<form class="in-table-cell mt1" action="/user/plugin/bgbilling/proto/bill.do">
	<input type="hidden" name="method" value="docTypeAdd"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>
	<input type="hidden" name="moduleId" value="${form.param.moduleId}"/>
	<input type="hidden" name="contractId" value="${form.param.contractId}"/>

	<div style="width: 100%;">
		<ui:combo-single list="${list.second}" hiddenName="typeIds" style="width: 100%;"/>
	</div>
	<div class="pl1">
		<ui:button type="add" onclick="if (this.form.typeIds.value) $$.ajax.post(this.form).done(() => $$.ajax.load('${form.requestUrl}', $('#${uiid}').parent()))"/>
	</div>
</form>