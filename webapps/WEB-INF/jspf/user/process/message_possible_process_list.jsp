<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="${form.httpRequestURI}">
	<html:hidden property="method"/>
	<html:hidden property="from"/>
	<html:hidden property="open"/>

	<c:forEach var="item" items="${form.getParamValuesListStr('object')}">
		<input type="hidden" name="object" value="${item}"/>
	</c:forEach>

	<c:set var="sendCommand">$$.ajax.load(this.form, $(this.form).parent())</c:set>

	<button type="button"
			onclick="this.form.open.value = this.form.open.value ? '' : 'true'; ${sendCommand}"
			class="mr1 btn-small ${form.param.open eq 'true' ? 'btn-blue' : 'btn-white'}">${l.l('Open only')}</button>

	<ui:page-control nextCommand="; ${sendCommand}"/>
</html:form>

<c:url var="updateProcessUrl" value="/user/message.do">
	<c:param name="method" value="messageUpdateProcess"/>
	<c:param name="messageTypeId" value="${form.param.messageTypeId}"/>
	<c:param name="messageId" value="${form.param.messageId}"/>
</c:url>

<table class="data mt1 hl">
	<tr>
		<td class="min">&nbsp;</td>
		<td>${l.l('Process')}</td>
		<td>${l.l('Status')}</td>
	</tr>

	<c:forEach var="item" items="${frd.list}">
		<c:set var="process" value="${item.first}"/>
		<c:set var="color" value="${item.second.color}"/>
		<%-- on empty color value expected to be ignored by browser --%>
		<tr style="background-color: ${color}">
			<td>
				<c:url var="url" value="${updateProcessUrl}">
					<c:param name="processId" value="${process.id}"/>
				</c:url>
				<button class="btn-white btn-small icon" onclick="
					if (confirm('${l.l('Link the message to the process?')}')) {
						$$.ajax
							.post('${url}')
							.done((result) => {
								$$.ajax.load('/user/message.do?&id=' + result.data.messageId, $('#${form.returnChildUiid}').parent());
							});
						return false;
					}
				"><i class="ti-link"></i></button>
			</td>
			<td><ui:process-link process="${process}"/></td>
			<td title="${ui.processCreatedAndClosed(l, process)}">${process.statusTitle}</td>
		</tr>
	</c:forEach>
</table>