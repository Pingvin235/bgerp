<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="plugin/bgbilling/proto/contract.do">
	<c:param name="action" value="getMemo"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
	<c:param name="returnChildUiid" value="${uiid}"/>
</c:url>	

<button type="button" class="btn-green" onclick="openUrlToParent('${url}', $('#${uiid}'))">+</button>

<table class="data mt1" style="width: 100%;" id="${uiid}">
	<tr>
		<td width="30">&nbsp;</td>
		<td width="100%">Тема</td>
		<td>Дата</td>
		<td>Пользователь</td>
	</tr>
	<c:forEach var="memo" items="${ form.response.data.memoList }" varStatus="status">
		<tr>
  			<c:url var="eUrl" value="${url}">
				<c:param name="id" value="${memo.id}"/>
			</c:url>
			<c:set var="editCommand" value="openUrlToParent('${eUrl}', $('#${uiid}') )"/>
			
			<c:url var="deleteAjaxUrl" value="plugin/bgbilling/proto/contract.do">
				<c:param name="action" value="deleteMemo"/>
				<c:param name="contractId" value="${form.param.conractId}"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="id" value="${memo.getId()}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}',$('#${uiid}'))"/>
			<td align="center" nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td>${memo.title}</td>
			<td nowrap="nowrap">${u:formatDate( memo.time, 'ymdhms' )}</td>
			<td nowrap="nowrap">${memo.user}</td>
		</tr>
	</c:forEach>
</table>
