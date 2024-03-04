<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<td ${tdClass}><input type="checkbox" name="processId" value="${process.id}"/></td>
	<td ${tdClass}><ui:process-link id="${process.id}"/></td>
	<td ${tdClass}>${ctxProcessTypeMap[process.typeId].title}</td>
	<td ${tdClass}>${ctxProcessStatusMap[process.statusId].title}</td>
	<td ${tdClass}>
		<ui:short-text text="${u:htmlEncode(process.description)}" maxLength="200"/>
	</td>
</tr>