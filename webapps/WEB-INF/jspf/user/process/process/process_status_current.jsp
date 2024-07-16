<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="statusEnd">
		 ${tu.format(process.statusTime, 'ymdhms')}
		<c:if test="${not empty process.statusChange.comment}">
			"${process.statusChange.comment}"
		</c:if>
		<c:if test="${process.statusUserId gt 0}">
			(<ui:user-link id="${process.statusUserId}"/>)
		</c:if>
	</c:set>

	<ui:when type="user">
		<c:set var="statusEdit" value="${ctxUser.checkPerm('ru.bgcrm.struts.action.ProcessAction:processStatusUpdate')}"/>

		${l.l('Status')}:
		[<b><%--
		--%><c:if test="${statusEdit}">
				<c:url var="url" value="${form.httpRequestURI}">
					<c:param name="method" value="processStatusEdit"/>
					<c:param name="id" value="${process.id}"/>
					<c:param name="returnUrl" value="${requestUrl}"/>
					<c:param name="returnChildUiid" value="${tableId}"/>
				</c:url>
				<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}').parent()); return false;"><%--
		--%></c:if>
			${process.statusTitle}
			<c:if test="${statusEdit}"></a></c:if>
		</b>]
		${statusEnd}
		<p:check action="ru.bgcrm.struts.action.ProcessAction:processStatusHistory">
			<c:url var="url" value="${form.httpRequestURI}">
				<c:param name="id" value="${process.id}"/>
				<c:param name="method" value="processStatusHistory"/>
				<c:param name="returnUrl" value="${requestUrl}"/>
			</c:url>
			[<a href="#" onclick="$$.ajax.load('${url}', $('#${tableId}').parent()); return false;">${l.l('log')}</a>]
		</p:check>
	</ui:when>

	<ui:when type="open">
		${l.l('Status')}:
		<b>${process.statusTitle}</b>
		${statusEnd}
	</ui:when>
</u:sc>