<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="title" value="${process.title}"/>

	<%-- если описание не содержит HTML разметки - оборачиваем его в <span class='title'> --%>
	<c:if test="${not title.contains( '<' ) }">
		<c:set var="title">
			<span class='title' id='process_title_${process.id}'>
				<c:set var="config" value="${ctxSetup.getConfig('org.bgerp.action.open.ProcessAction$Config')}"/>
				<c:if test="${config.isOpenForUser(process)}">
					<a target='_blank' href='${config.url(process)}' title='${l.l('Open Interface')}'>O</a>
				</c:if>
				${title}
			</span>
		</c:set>
	</c:if>

	<shell:title text="${title}"/>
	<shell:state text=""/>
</u:sc>