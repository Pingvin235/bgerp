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
			<c:import url="${stepData.step.jspFile}"/>
		</c:forEach>
	</c:if>

	<div class="mt1">
		<c:set var="closeScript">
			$('#processQueueShow').show();
			$('#processQueueEditProcess').hide();
			$$.ajax.load(formUrl($('#processQueueShow > #processQueueFilter')[0]).replace('page.pageIndex=1', 'page.pageIndex=-1'), $('#processQueueData'));
		</c:set>
		<c:if test="${process.id gt 0}">
			<button type="button" class="btn-white mr1" onclick="${closeScript}">Закрыть</button>
		</c:if>

		<c:set var="returnBreakCommand" value="${closeScript}"/>
		<c:set var="returnOkCommand" value="${closeScript}"/>
		<%@ include file="/WEB-INF/jspf/process_wizard_actions.jsp"%>
	</div>
</div>