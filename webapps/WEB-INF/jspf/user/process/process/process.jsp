<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="process" value="${form.response.data.process}" scope="request"/>
<c:set var="requestUrl" value="${form.requestUrl}" scope="request"/>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}"></div>

<c:choose>
	<%-- открытие где-то в списке процессов --%>
	<c:when test="${not empty form.returnUrl}">
		<c:set var="returnBreakCommand">openUrlToParent( '${form.returnUrl}', $('#${uiid}') )</c:set>
		<c:set var="returnOkCommand">${returnBreakCommand}</c:set>
	</c:when>
	<%-- открытие во вкладке вернего уровня --%>
	<c:otherwise>
		<%-- просто выход (в т.ч. после удаления временного процесса --%>
		<c:set var="returnBreakCommand">
			bgerp.closeObject = null;
			window.history.back();			
			bgerp.shell.removeCommandDiv('process-${process.id}');
		</c:set>
		<%-- выход после преобразования временного процесса в постоянный --%>
		<c:set var="returnOkCommand">
			bgerp.closeObject = null;
			bgerp.shell.removeCommandDiv('process-${process.id}');
			window.history.replaceState({href: 'process#${-process.id}'}, null, 'process#${-process.id}');
			bgerp.process.open(${-process.id});
		</c:set>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${empty wizardData}">	
		<c:set var="processTabsUiid" scope="request" value="${u:uiid()}"></c:set>
		<c:set var="customJsp" value="${processType.properties.configMap['processCardJsp']}"/>
		
		<c:if test="${not empty customJsp}">
			<jsp:include page="${customJsp}"/>
		</c:if>
		
		<c:if test="${empty customJsp}">
			<c:if test="${empty tableId}">
				<c:set var="tableId" value="${u:uiid()}"/>
			</c:if>
			
			<c:if test="${not empty process}">
				<%@ include file="process_editor.jsp"%>	
			</c:if>
		</c:if>	
	</c:when>
	<%-- мастер --%>
	<c:otherwise>
		<%-- класс нужен, чтобы не перезагружался редактор при переходе на вкладку, id - чтобы процесс не открылся как потерянный --%>
		<div class="editorStopReload" id="process-${process.id}"></div>
	
		<c:set var="reopenProcessEditorCode" scope="request">
			openUrlToParent( '${form.requestUrl}', $('#${uiid}') );
		</c:set>
		
		<c:set var="reopenProcessUrl" scope="request">
			${form.requestUrl}
		</c:set>
		
		<c:if test="${empty form.returnUrl}">
			<%@ include file="process_title.jsp"%>
		</c:if>	
		
		<div id="${uiid}" class="center1020">
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
				<%-- открытие где-то в списке процессов --%>
				<c:if test="${not empty form.returnUrl and process.id gt 0}">
					<button type="button" class="btn-grey mr1" onclick="${returnBreakCommand}">Закрыть</button>
				</c:if>
				
				<%@ include file="/WEB-INF/jspf/process_wizard_actions.jsp"%>
			</div>
		</div>
	</c:otherwise>
</c:choose>