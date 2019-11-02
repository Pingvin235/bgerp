<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

${l.l('Статус')}&nbsp;<b>${process.statusTitle}</b>: ${u:formatDate( process.statusTime, 'ymdhms' )} 
<c:if test="${not empty process.statusChange.comment}">
	"${process.statusChange.comment}"
</c:if>
<c:if test="${process.statusUserId gt 0}">
	<c:set var="userId" value="${process.statusUserId}"/>
	(<%@ include file="/WEB-INF/jspf/user_link.jsp"%>)
</c:if>

<p:check action="ru.bgcrm.struts.action.ProcessAction:processStatusHistory">
	<c:url var="url" value="process.do">
		<c:param name="id" value="${process.id}"/>
		<c:param name="returnUrl" value="${requestUrl}"/>
		<c:param name="action" value="processStatusHistory"/>
	</c:url>
	[<a href="#UNDEF" onclick="openUrlToParent( '${url}', $('#${tableId}') ); return false;">${l.l('история')}</a>]
</p:check>	