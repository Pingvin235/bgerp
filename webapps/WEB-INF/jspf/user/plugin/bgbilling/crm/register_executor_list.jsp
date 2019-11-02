<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="tableIndent" width="100%">
	<c:forEach var="registerExecutor" items="${form.response.data.registerExecutorList}">
		<tr>
			<td align="center">
				<input type="checkbox" name="executor" value="${ registerExecutor.getId() }"/>
			</td>
			<td width="100%">
				${ registerExecutor.getTitle() }
			</td>
		</tr>
	</c:forEach>
</table>
