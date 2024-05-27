<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="process" value="${frd.process}"/>
<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>
<c:set var="statusList" value="${u.getObjectList(ctxProcessStatusMap, processType.properties.statusIds)}"/>
<c:set var="allowedStatusIds" value="${process.allowedToChangeStatusIds}"/>

<h1>${l.l('Change status')}</h1>
<html:form action="/user/process">
	<input type="hidden" name="id" value="${process.id}"/>
	<input type="hidden" name="method" value="processStatusUpdate"/>

	<ui:combo-single
		prefixText="<b>${ctxProcessStatusMap[process.statusId].title}</b> ${l.l('status.to')}"
		style="width: 100%;" hiddenName="statusId">
		<jsp:attribute name="valuesHtml">
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

	<div class="mt1">
		${l.l('Комментарий')}
		<textarea name="comment" style="width: 100%; resize: vertical;" rows="4"></textarea>
	</div>

	<%@ include file="editor_save_cancel.jsp"%>
</html:form>


