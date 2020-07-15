<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table style="width: 100%;" class="data">
	<tr>
		<td width="30">&nbsp;</td>
		<td width="100%">Наименование</td>
	</tr>
	<c:forEach var="item" items="${list.first}">
		<tr>
			<td>
				<u:sc>
					<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/bill.do">
						<c:param name="action" value="docTypeDelete"/>
						<c:param name="typeIds" value="${item.id}"/>
						<c:param name="moduleId" value="${form.param.moduleId}"/>
						<c:param name="contractId" value="${form.param.contractId}"/>
						<c:param name="billingId" value="${form.param.billingId}"/>
					</c:url>
					<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}',$('#${uiid}'))"/>
					<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				</u:sc>
			</td>
			<td>${item.title}</td>
		</tr>
	</c:forEach>
</table>

<form class="in-table-cell mt1" action="/user/plugin/bgbilling/proto/bill.do">
	<input type="hidden" name="action" value="docTypeAdd"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>
	<input type="hidden" name="moduleId" value="${form.param.moduleId}"/>
	<input type="hidden" name="contractId" value="${form.param.contractId}"/>
	
	<div style="width: 100%;">
		<u:sc>
			<c:set var="list" value="${list.second}"/>	
			<c:set var="hiddenName" value="typeIds"/>
			<c:set var="style" value="width: 100%;"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
		</u:sc>	
	</div>	
	<div class="pl1">
		<button type="button" class="btn-grey" onclick="if( this.form.typeIds.value && sendAJAXCommand( formUrl( this.form ) ) ){ openUrlToParent('${form.requestUrl}',$('#${uiid}')) }">+</button>
	</div>
</form>		