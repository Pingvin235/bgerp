<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="tableIndent">
	<select name="contractAddressId" style="width: 100%">
			<c:forEach var="contractAddress" items="${form.response.data.contractAddressList}">
				 <option value="${ contractAddress.getId() }">${ contractAddress.getTitle() }</option>
			</c:forEach>
	</select>
</div>