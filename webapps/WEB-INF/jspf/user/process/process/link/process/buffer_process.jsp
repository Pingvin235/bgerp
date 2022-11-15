<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table class="data mt1">
	<tr>
		<td>&nbsp;</td>
		<td width="100%">${l.l('Title')}</td>
	</tr>
	<c:forEach var="process" items="${form.getSelectedValuesStr('process')}">
		<tr>
			<td>
				<form action="/user/link.do">
					<input type="hidden" name="action" value="addLink"/>
					<input type="hidden" name="objectType" value="process"/>
					<input type="hidden" name="id" value="${form.id}"/>
					<input type="hidden" name="linkedObjectType" value="${form.param['linkType']}"/>
					<input type="hidden" name="linkedObjectId" value="${su.substringBefore(process, ':')}"/>
					<input type="hidden" name="linkedObjectTitle" value=""/>
					<input type="checkbox" name="check"/>
				</form>
			</td>
			<td>
				${su.substringAfter(process, ':')}
			</td>
		</tr>
	</c:forEach>
</table>