<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<td ${tdClass}><input type="checkbox" name="processId" value="${process.id}"/></td>
	<td ${tdClass}><ui:process-link id="${process.id}"/></td>
	<td ${tdClass}>${process.type.title}</td>
	<td ${tdClass}>${process.statusTitle}</td>
	<td ${tdClass}>
		<ui:short-text text="${u:htmlEncode(process.description)}" maxLength="200"/>
	</td>
</tr>