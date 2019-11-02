<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="tableIndent">
	<select name="linkProblemId" style="width: 100%" multiline size="12">
		<option value="" selected></option>
		<c:forEach var="callCurrentProblem" items="${form.response.data.callCurrentProblemList}">
			 <option value="${ callCurrentProblem.getId() }">${ callCurrentProblem.getTitle() }</option>
		</c:forEach>
	</select>
</div>