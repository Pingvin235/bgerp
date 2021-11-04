<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>
	<c:set var="statusList" value="${u:orderedObjectList( ctxProcessStatusMap, processType.properties.statusIds ) }"/>
	<c:set var="allowedStatusIds" value="${process.allowedToChangeStatusIds}"/>

	<c:set var="statusEditorUiid" value="${u:uiid()}"/>
	<div id="${statusEditorUiid}" class="mt1">
		<html:form action="/user/process" styleId="statusEdit">
			<input type="hidden" name="id" value="${process.id}"/>
			<input type="hidden" name="action" value="processStatusUpdate"/>

			<c:set var="categoryParamId">${processType.properties.configMap['categoryParamId']}</c:set>

			<c:set var="statusSelectUiid" value="${u:uiid()}"/>

			<c:url var="categoryEditorUrl" value="/user/empty.do">
				<c:param name="forwardFile" value="/WEB-INF/jspf/user/process/process/process_status_category_editor.jsp"/>
				<c:param name="typeId" value="${processType.id}"/>
				<c:param name="processId" value="${process.id}"/>
				<c:param name="statusEditorUiid" value="${statusEditorUiid}"/>
				<c:param name="statusSelectUiid" value="${statusSelectUiid}"/>
				<c:param name="requestFormUrl" value="${requestUrl}"/>

				<c:param name="returnChildUiid" value="${tableId}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>

			<ui:combo-single
				id="${statusSelectUiid}" prefixText="${l.l('Переключить статус в')}:"
				style="width: 100%;" styleTextValue="width: 100%;"
				hiddenName="statusId" value=""
				onSelect="
					$$.ajax
						.load('${categoryEditorUrl}'+'&statusId='+ $hidden.val(), $('#${statusEditorUiid} div#editorCat'))
						.done(() => {
							$('#${statusEditorUiid} div[type=editor]').show();
						})
				">
				<jsp:attribute name="valuesHtml">
					<%-- first empty element to do not show real status --%>
					<li style="display: none;"></li>
					<c:forEach var="item" items="${statusList}">
						<c:if test="${allowedStatusIds.contains(item.id) and item.id ne process.statusId}">
							<li value="${item.id}">${item.title}</li>
						</c:if>
					</c:forEach>
					<c:if test="${process.createTime ne process.statusTime}">
						<li value="prev">** ${l.l('ПРЕДЫДУЩИЙ')} **</li>
					</c:if>
				</jsp:attribute>
			</ui:combo-single>

			<div id="editor" type="editor" style="display: none;" class="mt1">
				${l.l('Комментарий')}:
				<textarea name="comment" style="width: 100%; resize: vertical;" rows="4"></textarea>
			</div>

		</html:form>

		<div id="editorCat" type="editor" style="width: 100%;" class="tableIndentVertical">
		</div>
	</div>
</u:sc>
