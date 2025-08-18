<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="process" value="${frd.process}" scope="request"/>

<c:set var="reopenProcessEditorCode" scope="request">
	$$.ajax.load('${form.requestUrl}', $('#processQueueEditProcess'));
</c:set>

<c:set var="reopenProcessUrl" scope="request">
	${form.requestUrl}
</c:set>

<div id="processEditor-${process.id}">
	<c:if test="${not empty wizardData}">
		<c:forEach var="stepData" items="${wizardData.stepDataList}">
			<c:set var="stepData" value="${stepData}" scope="request"/>
			<c:if test="${not empty stepData.step.title}">
				<h2>${stepData.step.title}</h2>
			</c:if>
			<c:import url="${stepData.step.jsp}"/>
		</c:forEach>
	</c:if>

	<div class="mt1">
		<c:set var="closeScript">
			$('#processQueueShow').show();
			$('#processQueueEditProcess').hide();
			const form = $('#processQueueShow > #processQueueFilter')[0];
			form.elements['page.pageIndex'].value = -1;
			$$.ajax.load(form, $('#processQueueData'));
		</c:set>
		<c:if test="${process.id gt 0}">
			<button type="button" class="btn-white mr1" onclick="${closeScript}">${l.l('Close')}</button>
		</c:if>

		<jsp:include page="/WEB-INF/jspf/process_wizard_actions.jsp">
			<jsp:param name="returnBreakCommand" value="${closeScript}"/>
			<jsp:param name="returnOkCommand" value="${closeScript}"/>
		</jsp:include>
	</div>
</div>