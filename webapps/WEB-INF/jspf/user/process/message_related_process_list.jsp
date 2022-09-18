<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/user/process">
	<html:hidden property="action"/>
	<html:hidden property="from"/>
	<html:hidden property="open"/>

	<c:forEach var="item" items="${form.getSelectedValuesListStr('object')}">
		<input type="hidden" name="object" value="${item}"/>
	</c:forEach>

	<c:set var="sendCommand">$$.ajax.load(this.form, $(this.form).parent())</c:set>

	<button type="button"
			onclick="this.form.open.value = this.form.open.value ? '' : 'true'; ${sendCommand}"
			class="mr1 ${form.param.open eq 'true' ? 'btn-blue' : 'btn-white'}">${l.l('Только открытые')}</button>

	<c:set var="nextCommand" value="; ${sendCommand}"/>
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<c:url var="updateProcessUrl" value="/user/message.do">
	<c:param name="action" value="messageUpdateProcess"/>
	<c:param name="messageTypeId" value="${form.param.messageTypeId}"/>
	<c:param name="messageId" value="${form.param.messageId}"/>
</c:url>

<table class="data mt1 hl">
	<tr>
		<td>ID</td>
		<td>${l.l('Описание')}</td>
		<td>${l.l('Статус')}</td>
		<td>${l.l('Создан')}</td>
	</tr>

	<c:forEach var="item" items="${form.response.data.list}">
		<c:set var="color" value="${item.second.color}"/>
		<%-- on empty color value expected to be ignored by browser --%>
		<tr style="background-color: ${color}">
			<c:set var="process" value="${item.first}"/>
			<td>
				<a href="#" onclick="$$.process.open(${process.id}); return false;">${process.id}</a>
				<c:url var="url" value="${updateProcessUrl}">
					<c:param name="processId" value="${process.id}"/>
				</c:url>
				[<a href="#" onclick="
					$$.ajax
						.post('${url}')
						.done(() => {
							$$.ajax.load('${form.returnUrl}', $('#${form.returnChildUiid}').parent());
						});
					return false;
				">${l.l('set')}</a>]
			</td>
			<td>${process.description}</td>
			<td>${process.statusTitle}</td>
			<td>${tu.format(process.createTime, 'ymdhms')}</td>
		</tr>
	</c:forEach>
</table>