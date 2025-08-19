<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="process" value="${frd.process}" scope="request"/>
<c:set var="requestUrl" value="${form.requestUrl}" scope="request"/>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}"></div>

<c:choose>
	<%-- открытие где-то в списке процессов --%>
	<c:when test="${not empty form.returnUrl}">
		<c:set var="returnBreakCommand">$$.ajax.load('${form.returnUrl}', $('#${uiid}').parent())</c:set>
		<c:set var="returnOkCommand">${returnBreakCommand}</c:set>
	</c:when>
	<%-- открытие в оснастке вернего уровня --%>
	<c:otherwise>
		<%-- просто выход, в т.ч. после удаления временного процесса --%>
		<c:set var="returnBreakCommand">
			$$.process.remove(${process.id});
			window.history.back();
		</c:set>
		<%-- выход после преобразования временного процесса в постоянный in wizard --%>
		<c:set var="returnOkCommand">
			$$.process.remove(${process.id});
			window.history.replaceState({href: 'process#${-process.id}'}, null, 'process#${-process.id}');
			$$.process.open(${-process.id});
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
	<%-- wizard --%>
	<c:otherwise>
		<%-- класс нужен, чтобы не перезагружался редактор при переходе на вкладку, id - чтобы процесс не открылся как потерянный --%>
		<div class="editorStopReload" id="process-${process.id}"></div>

		<%-- used in wizard --%>
		<c:set var="reopenProcessEditorCode" scope="request">
			$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());
		</c:set>

		<c:set var="reopenProcessUrl" scope="request">
			${form.requestUrl}
		</c:set>

		<c:if test="${empty form.returnUrl}">
			<%@ include file="process_title.jsp"%>
		</c:if>

		<div id="${uiid}" class="center1020">
			<c:forEach var="stepData" items="${wizardData.stepDataList}">
				<c:set var="stepData" value="${stepData}" scope="request"/>
				<c:if test="${not empty stepData.step.title}">
					<h2>${stepData.step.title}</h2>
				</c:if>
				<c:import url="${stepData.step.jsp}"/>
			</c:forEach>

			<div class="mt1">
				<%-- открытие где-то в списке процессов --%>
				<c:if test="${not empty form.returnUrl and process.id gt 0}">
					<button type="button" class="btn-grey mr1" onclick="${returnBreakCommand}">${l.l('Close')}</button>
				</c:if>

				<jsp:include page="/WEB-INF/jspf/process_create_wizard_end.jsp">
					<jsp:param name="returnBreakCommand" value="${returnBreakCommand}"/>
					<jsp:param name="returnOkCommand" value="${returnOkCommand}"/>
				</jsp:include>
			</div>
		</div>
	</c:otherwise>
</c:choose>