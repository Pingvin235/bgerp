<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="tableIndent">
	<select name="registerGroupId" style="width: 100%" ${ form.param.multiline } 
		onchange="bgbilling_getRegistredExecutors('${form.param.billingId}-${form.param.contractId}-registerExecutorList','${form.param.billingId}',$(this).children(':selected').val(),0);">
			<c:forEach var="registerGroup" items="${form.response.data.registerGroupList}">
				 <option value="${ registerGroup.getId() }">${ registerGroup.getTitle() }</option>
			</c:forEach>
	</select>
</div>