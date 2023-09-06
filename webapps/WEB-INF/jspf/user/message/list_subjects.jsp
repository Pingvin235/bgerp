<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<%-- table ID --%>
	<c:set var="uiid" value="${u:uiid()}"/>

	<c:choose>
		<c:when test="${form.param.processed eq 1}">
			<table class="data hl fixed-header" id="${uiid}">
				<tr>
					<td width="30">ID</td>
					<td>${l.l('Type')}</td>
					<td>${l.l('Тема')}</td>
					<td>${l.l('От')}</td>
					<td>${l.l('Время')}</td>
					<td>${l.l('Process')}</td>
				</tr>

				<c:forEach var="item" items="${form.response.data.list}">
					<c:url var="url" value="/user/message.do">
						<c:param name="id" value="${item.id}"/>
					</c:url>

					<tr openUrl="${url}">
						<td>${item.id}</td>
						<td>${config.typeMap[item.typeId].title}</td>
						<td>${item.subject}</td>
						<td>${item.from}</td>
						<td>${tu.format(item.fromTime, 'ymdhm')}</td>
						<td><ui:process-link id="${item.process.id}" text="${item.process.id}"/></td>
					</tr>
				</c:forEach>
			</table>
		</c:when>
		<c:otherwise>
			<form action="/user/message.do">
				<input type="hidden" name="action" value="messageDelete"/>

				<c:set var="menuUiid" value="${u:uiid()}"/>
				<c:set var="selectedUiid" value="${u:uiid()}"/>

				<ui:popup-menu id="${menuUiid}">
					<li><a href="#" onclick="$$.table.select($('#${uiid}'), $('#${selectedUiid}'), 'all'); return false;"><i class="ti-check-box"></i> ${l.l("Select All")}</a></li>
					<li><a href="#" onclick="$$.table.select($('#${uiid}'), $('#${selectedUiid}'), 'nothing'); return false;"><i class="ti-control-stop"></i> ${l.l("Deselect All")}</a></li>
					<li><a href="#" onclick="$$.table.select($('#${uiid}'), $('#${selectedUiid}'), 'invert'); return false;"><i class="ti-control-shuffle"></i> ${l.l("Invert Selection")}</a></li>
					<%-- script var is provided by parent JSP --%>
					<li><a href="#" onclick="
							if (!($('#${selectedUiid}').text() > 0) || !confirm('${l.l("Удалить выбранные")}?'))
								return false;
							$$.ajax.post($(this).closest('form')).done(() => { ${script} });
							return false;
						"><i class="ti-trash"></i> ${l.l('Удалить выбранные')} [<span id="${selectedUiid}"></span>]</a></li>
				</ui:popup-menu>

				<table class="data hl fixed-header" id="${uiid}">
					<tr>
						<td width="30">
							<ui:button type="more" styleClass="btn-small" onclick="$$.ui.menuInit($(this), $('#${menuUiid}'), 'left', true);"/>
						</td>
						<c:if test="${form.param.typeId le 0}">
							<td>${l.l('Type')}</td>
						</c:if>
						<td>${l.l('Тема')}</td>
						<td>${l.l('От')}</td>
						<td>${l.l('Время')}</td>
					</tr>

					<c:set var="today" value="<%=new java.util.Date()%>"/>

					<c:forEach var="item" items="${form.response.data.list}">
						<c:url var="url" value="/user/message.do">
							<c:param name="typeId" value="${item.typeId}"/>
							<c:param name="messageId" value="${item.systemId}"/>
						</c:url>

						<tr valign="top" openUrl="${url}">
							<td style="text-align: center;">
								<input type="checkbox" name="typeId-systemId" value="${item.typeId}-${item.systemId}" title="${l.l("Keep Shift pressed for range selection")}"/>
							</td>
							<c:if test="${form.param.typeId le 0}">
								<td>${config.typeMap[item.typeId].title}</td>
							</c:if>
							<td>${item.subject}</td>
							<%-- TODO: support notes by link to author user --%>
							<td title="${item.from}">${item.from}</td>
							<td nowrap="nowrap">
								${tu.daysDelta(today, item.fromTime) eq 0 ?
									tu.format(item.fromTime, 'HH:mm') :
									tu.format(item.fromTime, 'ymdhm')
								}
							</td>
						</tr>
					</c:forEach>
				</table>
			</form>
		</c:otherwise>
	</c:choose>

	<script>
		$$.message.subjectTableInit('${uiid}', '${editorUiid}', '${selectedUiid}');
	</script>
</u:sc>