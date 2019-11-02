<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="tableIndent">
	<select name="taskTypeId" style="width: 100%">
			<c:forEach var="taskType" items="${form.response.data.taskTypeList}">
				 <option value="${ taskType.getId() }">${ taskType.getTitle() }</option>
			</c:forEach>
	</select>
</div>