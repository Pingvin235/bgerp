<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="title">
		<%@ include file="process_title_reference.jsp"%>
	</c:set>

	<%-- если описание не содержит HTML разметки - оборачиваем его в <span class='title'> --%>
	<c:if test="${not title.contains( '<' ) }">
		<c:set var="title">
			<span class='title' id='process_title_${process.id}'>
				<c:set var="config" value="${u:getConfig(ctxSetup, 'org.bgerp.action.open.ProcessAction$Config')}"/>
				<c:if test="${config.getProcessTypeIds().contains(process.typeId)}">
					<a target='_blank' href='/open/process/${process.id}' title='${l.l('Открытый интерфейс')}'>O</a>
				</c:if>
				${title}
			</span>
		</c:set>
	</c:if>

	<shell:title text="${title}"/>
	<shell:state text=""/>
</u:sc>