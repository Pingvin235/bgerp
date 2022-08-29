<%@ tag pageEncoding="UTF-8" description="Report's column headers tr"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="data" type="org.bgerp.plugin.report.model.Data" description="Report data"%>

<tr>
	<c:forEach var="c" items="${data.action.columns.visibleColumns}">
		<td>${c.getTitle(l)}</td>
	</c:forEach>
</tr>