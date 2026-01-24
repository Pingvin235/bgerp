<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>${l.l('Создать Backup')}</h2>
<form action="/admin/plugin/backup/backup.do">
	<input type="hidden" name="method" value="backup"/>
	<ui:combo-single name="db" widthTextValue="2em" prefixText="${l.l('Включить БД')}:">
		<jsp:attribute name="valuesHtml">
			<li value="0">${l.l('No')}</li>
			<li value="1">${l.l('Yes')}</li>
		</jsp:attribute>
	</ui:combo-single>
	<ui:button type="run" styleClass="ml1" onclick="
		$$.ajax.post(this).done(() => {
			$$.ajax.loadContent('${form.requestUrl}', this);
		});"/>
</form>

<h2>${l.l('Files')}</h2>

<c:set var="files" value="<%=org.bgerp.plugin.svc.backup.action.admin.BackupAction.FILE_BACKUP%>"/>

<c:set var="deletionAllowed" value="${ctxUser.checkPerm(files.deletePermissionAction)}"/>
<c:set var="restoreAllowed" value="${ctxUser.checkPerm('org.bgerp.plugin.svc.backup.action.admin.BackupAction:restore')}"/>

<c:set var="cleanupCandidates" value="${config.cleanupCandidates(files)}"/>

<form action="${files.deleteURL}">
	<table class="data hl">
		<tr>
			<c:if test="${deletionAllowed}">
				<td width="1em">
					<ui:button type="del" styleClass="btn-small" onclick="
						$$.ajax.post(this).done(() => $$.ajax.loadContent('${form.requestUrl}', this))
					"/>
				</td>
			</c:if>
			<td width="50%">${l.l('File')}</td>
			<td width="50%">${l.l('Modification time')}</td>
			<td>${l.l('Size')}</td>
			<c:if test="${restoreAllowed}">
				<td width="1em"></td>
			</c:if>
		</tr>
		<c:forEach var="file" items="${files.list()}">
			<tr>
				<c:if test="${deletionAllowed}">
					<td style="text-align: center;">
						<input type="checkbox" name="name" value="${file.name}" ${cleanupCandidates.contains(file.name) ? 'checked' : ''}/>
					</td>
				</c:if>
				<td>
					<c:set var="a">
						<c:if test="${ctxUser.checkPerm(files.downloadPermissionAction)}">
							<c:url var="url" value="${files.downloadURL}">
								<c:param name="name">${file.name}</c:param>
							</c:url>
							<a href="${url}">
						</c:if>
					</c:set>
					${a}
					${file.name}
					<c:if test="${not empty a}"></a></c:if>
				</td>
				<td>${tu.format(tu.convertLongToTimestamp(file.lastModified()), 'ymdhms')}</td>
				<td nowrap>${fu.byteCountToDisplaySize(file.length())}</td>
				<c:if test="${restoreAllowed}">
					<td nowrap>
						<c:if test="${restoreAllowed}">
							<c:url var="url" value="/admin/plugin/backup/backup.do">
								<c:param name="method" value="restore"/>
								<c:param name="name">${file.name}</c:param>
							</c:url>
							<button type="button" class="btn-white btn-small icon" onclick="
									if (!confirm('${l.l('Do you really want to completely rewrite your DB and app files?')}')) return;
									$$.ajax
										.post('${url}', {control: this, failAlert: false})
										.fail(() => {alert('${l.l('Restart has been done, refresh the browser site')}')});
								"><i class="ti-share"></i>
							</button>
						</c:if>

					</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
</form>

<shell:title text="Backup"/>
