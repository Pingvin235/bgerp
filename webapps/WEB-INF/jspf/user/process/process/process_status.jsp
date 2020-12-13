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

			<u:sc>
				<c:set var="valuesHtml">
					<c:forEach var="item" items="${statusList}">
						<c:if test="${u:contains( allowedStatusIds, item.id ) and item.id ne process.statusId}">
							<li value="${item.id}">${item.title}</li>
						</c:if>
					</c:forEach>
					<c:if test="${process.createTime ne process.statusTime}">
						<li value="prev">** ${l.l('ПРЕДЫДУЩИЙ')} **</li>
					</c:if>
				</c:set>
				<c:set var="id" value="${statusSelectUiid}"/>
				<c:set var="hiddenName" value="statusId"/>
				<c:set var="value" value="-1"/>
				<c:set var="prefixText">
					${l.l('Переключить из статуса ')}&quot;${process.statusTitle}&quot; ${l.l('в')}:
				</c:set>
				<c:set var="style" value="width: 100%;"/>
				<c:set var="styleTextValue" value="width: 100%;"/>
				<c:set var="onSelect" value="openUrlTo( '${categoryEditorUrl}'+'&statusId='+ $hidden.val(), $('#${statusEditorUiid} div#editorCat') );$('#${statusEditorUiid} div[type=editor]').show();"/>
				<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
			</u:sc>

			<div id="editor" type="editor" style="display: none;" class="mt1">
				${l.l('Комментарий')}:
				<textarea name="comment" style="width: 100%; resize: vertical;" rows="4"></textarea>
			</div>

		</html:form>

		<div id="editorCat" type="editor" style="width: 100%;" class="tableIndentVertical">
		</div>
	</div>
</u:sc>
