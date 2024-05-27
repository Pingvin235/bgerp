<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}" class="tableIndent">
	<c:choose>
		<c:when test="${stepData.filled}">
			${stepData.step.config['continuedText'] }
		</c:when>
		<c:otherwise>
			<html:form action="/user/parameter">
				<input type="hidden" name="id" value="${stepData.processId}"/>
				<input type="hidden" name="method" value="parameterUpdate"/>
				<input type="hidden" name="paramId" value="${stepData.step.param.id}"/>
				<input type="hidden" name="value" value="1"/>

				<input type="button" onclick="$$.ajax.post(this.form).done(() => { ${reopenProcessEditorCode} })" value="${stepData.step.config['continueText'] }"/>
			</html:form>
		</c:otherwise>
	</c:choose>
</div>
