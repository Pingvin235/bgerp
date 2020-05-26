<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="perm" value="${p:get( form.user.id, 'ru.bgcrm.struts.action.LinkAction:deleteLink' )}"/>
	<c:set var="allowLinkDelete" value="${perm['allowProcessLinkDelete'] ne 0}"/>

	<c:set var="uiid" value="${u:uiid()}"/>
	<c:set var="rows" value="2"/>
	<%@ include file="/WEB-INF/jspf/table_row_edit_mode.jsp"%>

	<c:set var="adoeb" value="${allowLinkDelete or editButton}"/>

	<table class="hdata" style="width: 100%;" id="${uiid}">
		<tr class="header">
			<c:if test="${adoeb}">
				<td>&nbsp;</td>
			</c:if>
			<td>ID</td>
			<td>Время создания</td>
			<td>Тип</td>
			<td>Статус</td>
		</tr>
		<tr class="header">
			<c:choose>
				<c:when test="${adoeb}"><td colspan="5"></c:when>
				<c:otherwise><td colspan="4"></c:otherwise>
			</c:choose>
			Описание</td>
		</tr>

		<c:forEach var="item" items="${list}">
			<c:set var="process" value="${item.second}"/>

			<c:url var="url" value="/user/process.do">
				<c:param name="action" value="process"/>
				<c:param name="id" value="${item.second.id}"/>
				<c:param name="mode" value="linked"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>

			<tr openUrl="${url}">
				<c:if test="${adoeb}">
					<td nowrap="nowrap" >
						<c:if test="${editButton}">
							<u:sc>
								<c:set var="command" value="openUrlToParent( '${url}', $('#${uiid}') )"/>
								<button type="button" class="btn-white btn-small" onclick="${command}" title="Открыть">*</button>
							</u:sc>
						</c:if>

						<c:if test="${allowLinkDelete}">
							<c:url var="deleteAjaxUrl" value="link.do" scope="request">
								<c:param name="action" value="deleteLink"/>
								<c:param name="objectType" value="process"/>
								<c:param name="linkedObjectType" value="${item.first}"/>

								<c:choose>
									<c:when test="${mode eq 'linked'}">
										<c:param name="id" value="${process.id}"/>
										<c:param name="linkedObjectId" value="${form.id}"/>
									</c:when>
									<c:otherwise>
										<c:param name="id" value="${form.id}"/>
										<c:param name="linkedObjectId" value="${process.id}"/>
									</c:otherwise>
								</c:choose>
							</c:url>

							<c:choose>
								<c:when test="${not empty afterEditLinkCommand}">
									<c:set var="deleteAjaxCommandAfter">${afterEditLinkCommand}</c:set>
								</c:when>
								<c:otherwise>
									<c:set var="deleteAjaxCommandAfter" scope="request">openUrlToParent( '${form.requestUrl}', $('#${uiid}') );</c:set>
								</c:otherwise>
							</c:choose>
						</c:if>
						<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
					</td>
				</c:if>
				<td nowrap="nowrap">
					<ui:process-link id="${process.id}"/>

					<c:set var="type" value="${item.first}"/>
					<%@ include file="process_link_type.jsp"%>
				</td>
				<td nowrap="nowrap">${u:formatDate( process.createTime, 'ymdhms')}</td>
				<td nowrap="nowrap">${ctxProcessTypeMap[process.typeId]}</td>
				<td nowrap="nowrap">${ctxProcessStatusMap[process.statusId]}</td>
			</tr>
			<tr openUrl="${url}">
				<c:choose>
					<c:when test="${adoeb}"><td colspan="5"></c:when>
					<c:otherwise><td colspan="4"></c:otherwise>
				</c:choose>

				<c:set var="text">
					<c:choose>
						<c:when test="${not empty process.reference}">
							${process.reference}
						</c:when>
						<c:otherwise>
							<%-- convert tag symbols line breaks --%>
							${u:htmlEncode(process.description)}
						</c:otherwise>
					</c:choose>
				</c:set>
				<c:set var="maxLength" value="200"/>
				<%@include file="/WEB-INF/jspf/short_text.jsp"%>
				</td>
			</tr>
		</c:forEach>
	</table>
</u:sc>
