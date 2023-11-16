<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<td ${tdClass}><input type="checkbox" name="processId" value="${process.id}"/></td>
	<td ${tdClass}><ui:process-link id="${process.id}"/></td>
	<td ${tdClass}>${ctxProcessTypeMap[process.typeId]}</td>
	<td ${tdClass}>${ctxProcessStatusMap[process.statusId]}</td>
	<td ${tdClass}>
		<u:sc>
			<c:set var="text" value="${u:htmlEncode(process.description)}"/>
			<c:set var="maxLength" value="200"/>
			<%@include file="/WEB-INF/jspf/short_text.jsp"%>
		</u:sc>
	</td>
</tr>