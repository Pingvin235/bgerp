<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="editType" value="${ctxUser.checkPerm('ru.bgcrm.struts.action.ProcessAction:processTypeEdit')}"/>
	<c:if test="${editType}">
		<c:url var="url" value="/user/process.do">
			<c:param name="method" value="processTypeEdit"/>
			<c:param name="id" value="${process.id}"/>
			<c:param name="typeId" value="${process.typeId}" />
			<c:param name="returnUrl" value="${requestUrl}"/>
			<c:param name="returnChildUiid" value="${tableId}"/>
			<c:param name="forward" value="processTypeChange"/>
		</c:url>
		[<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}').parent()); return false;"><%--
--%></c:if><%--
--%><b><%--
	--%><c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>
		<c:choose>
			<c:when test="${not empty processType}">
				<c:forEach var="item" items="${processType.path}" varStatus="status">
					<c:if test="${status.index ne 0}"> / </c:if>
						${item.title}
				</c:forEach>
			</c:when>
			<c:otherwise>
				${l.l('Данный тип процесса был удален')} (${process.typeId})
			</c:otherwise>
		</c:choose><%--
--%></b><%--
--%><c:if test="${editType}">
		</a>]
	</c:if>
</u:sc>