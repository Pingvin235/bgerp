<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${process.id le 0}">
	<c:set var="command">
		if (confirm('${l.l('Прервать создание процесса?')}')) {
			const command = 'process.do?method=processDeleteTmp&id=${process.id}';
			$$.ajax.post(command).done(() => {
				${returnBreakCommand}
			})
		}
	</c:set>
	<button type="button" class="btn-grey mr2" onclick="${command}">${l.l('Прервать создание')}</button>

	<c:if test="${wizardData.allFilled}">
		<c:set var="command">
			const command = 'process.do?method=processFinishCreateTmp&id=${process.id}';
			$$.ajax.post(command).done((result) => {
				${returnOkCommand}
			})
		</c:set>
		<button type="button" class="btn-grey" onclick="${command}">${l.l('Завершить')}</button>
	</c:if>
</c:if>