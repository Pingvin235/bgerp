<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${process.id le 0}">
	<c:set var="command">
		if (confirm('${l.l('Прервать создание процесса?')}')) {
			var command = 'process.do?action=processDeleteTmp&id=${process.id}';
			if (sendAJAXCommand(command)) {
				${returnBreakCommand}
			}
		}
	</c:set>
	<button type="button" class="btn-grey mr2" onclick="${command}">${l.l('Прервать создание')}</button>
	
	<c:if test="${wizardData.allFilled}">
		<c:set var="command">
			var command = 'process.do?action=processFinishCreateTmp&id=${process.id}';
			var result = sendAJAXCommand(command);
			if (result) {
				${returnOkCommand}
			}
		</c:set>
		<button type="button" class="btn-grey" onclick="${command}">${l.l('Завершить')}</button>
	</c:if>
</c:if>