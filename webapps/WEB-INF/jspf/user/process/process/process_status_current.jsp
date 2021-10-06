<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

${l.l('Статус')}&nbsp;<b>${process.statusTitle}</b>: ${tu.format( process.statusTime, 'ymdhms' )} 
<c:if test="${not empty process.statusChange.comment}">
	"${process.statusChange.comment}"
</c:if>
<c:if test="${process.statusUserId gt 0}">
	(<ui:user-link id="${process.statusUserId}"/>)
</c:if>

<ui:when type="user">
	<p:check action="ru.bgcrm.struts.action.ProcessAction:processStatusHistory">
		<c:url var="url" value="/user/process.do">
			<c:param name="id" value="${process.id}"/>
			<c:param name="returnUrl" value="${requestUrl}"/>
			<c:param name="action" value="processStatusHistory"/>
		</c:url>
		[<a href="#" onclick="openUrlToParent( '${url}', $('#${tableId}') ); return false;">${l.l('история')}</a>]
	</p:check>
</ui:when>