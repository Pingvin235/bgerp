<%@ tag pageEncoding="UTF-8" description="File list"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="files" required="true" type="org.bgerp.util.Files" description="Files object"%>
<%@ attribute name="maxCount" type="java.lang.Integer" description="Maximum count of shown files"%>
<%@ attribute name="action" description="Action for file operations"%>

<table class="data">
	<tr>
		<td>${l.l('Файл')}</td>
		<td>${l.l('Время изменения')}</td>
		<td>${l.l('Размер')}</td>
	</tr>
	<c:forEach var="file" items="${files.list()}" varStatus="status">
		<c:if test="${empty maxCount or status.count le maxCount}">
			<tr>
				<td>
					<c:set var="a">
						<p:check action="${files.downloadPermissionAction}">
							<c:url var="url" value="${files.downloadURL}">
								<c:param name="name">${file.name}</c:param>
							</c:url>
							<a href="${url}">
						</p:check>
					</c:set>
					${a}
					${file.name}
					<c:if test="${not empty a}"></a></c:if>
				</td>
				<td>${tu.format(tu.convertLongToTimestamp(file.lastModified()), 'ymdhms')}</td>
				<td>${fu.byteCountToDisplaySize(file.length())}</td>
			</tr>
		</c:if>
		<%-- TODO: Write the whole amount of files with button to remove them --%>
	</c:forEach>
</table>