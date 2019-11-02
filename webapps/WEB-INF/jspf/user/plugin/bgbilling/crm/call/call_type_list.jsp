<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="tableIndent">
	<select name="typeId" style="width: 100%" multiline size="10" onchange="bgbilling_updateGroupId('${form.param.billingId}','${form.param.contractId}' ,$(this).children(':selected').val());">
			<c:forEach var="callType" items="${form.response.data.callTypeList}">
				 <option value="${ callType.getId() }">${ callType.getTitle() }</option>
			</c:forEach>
	</select>
	</br>
	<b>Описание звонка</b>
	<div class="box" style="overflow: auto; width: inherit; height: 50px;">
		<textarea rows="10" cols="300" style="width:100%; height:100%;" 
			onChange="$('#${form.param.billingId}-${form.param.contractId}-createCallform input[name=comment]').val( $(this).val() );"/>
	</div>
</div>