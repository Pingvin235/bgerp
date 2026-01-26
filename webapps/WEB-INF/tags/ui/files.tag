<%@ tag pageEncoding="UTF-8" description="File list"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="files" required="true" type="org.bgerp.app.servlet.file.Files" description="Files object"%>
<%@ attribute name="maxCount" type="java.lang.Integer" description="Maximum count of shown files"%>
<%@ attribute name="requestUrl" description="Request URL for reloading, required when deletion is enabled"%>

<c:set var="deletionEnabled" value="${files.options.deletionEnabled and not empty requestUrl and ctxUser.checkPerm(files.deletePermissionAction)}"/>

<c:set var="fileList" value="${files.list()}"/>

<table class="data hl">
	<tr>
		<td width="50%">${l.l('File')} [${fileList.size()}]</td>
		<td width="50%">${l.l('Modification time')}</td>
		<td nowrap>${l.l('Size')} [${fu.byteCountToDisplaySize(files.size(fileList))}]</td>
		<c:if test="${deletionEnabled}">
			<td></td>
		</c:if>
	</tr>
	<c:forEach var="file" items="${fileList}" varStatus="status">
		<c:if test="${empty maxCount or status.count le maxCount}">
			<tr>
				<td>
					<c:set var="a">
						<c:choose>
							<c:when test="${file.file}">
								<c:if test="${files.options.downloadEnabled and ctxUser.checkPerm(files.downloadPermissionAction)}">
									<c:url var="url" value="${files.downloadURL}">
										<c:param name="name">${file.name}</c:param>
									</c:url>
									<a href="${url}">
								</c:if>
							</c:when>
							<c:otherwise>
								<%-- TODO: Directory link. --%>
							</c:otherwise>
						</c:choose>
					</c:set>
					${a}
					<c:set var="highlighter" value="${files.options.highlighter(file)}"/>
					<c:choose>
						<c:when test="${empty highlighter}">${file.name}</c:when>
						<c:otherwise>
							<c:url var="url" value="${files.highlightURL}">
								<c:param name="name">${file.name}</c:param>
							</c:url>
							<c:set var="uiid" value="${u:uiid()}"/>
							<span id="${uiid}">${file.name}</span>
							<script>$$.hlFile('${uiid}', '${url}')</script>
						</c:otherwise>
					</c:choose>
					<c:if test="${not empty a}"></a></c:if>
				</td>
				<td>${tu.format(tu.convertLongToTimestamp(file.lastModified()), 'ymdhms')}</td>
				<td nowrap>${fu.byteCountToDisplaySize(file.length())}</td>
				<c:if test="${deletionEnabled}">
					<td>
						<c:if test="${ctxUser.checkPerm(files.deletePermissionAction)}">
							<c:url var="url" value="${files.deleteURL}">
								<c:param name="name">${file.name}</c:param>
							</c:url>
							<ui:button type="del" styleClass="btn-small"
								onclick="$$.ajax.post('${url}', {control: this}).done(() => $$.ajax.loadContent('${requestUrl}', this))"/>
						</c:if>
					</td>
				</c:if>
			</tr>
		</c:if>
		<%-- TODO: Write the whole amount of files with button to remove them --%>
	</c:forEach>
</table>