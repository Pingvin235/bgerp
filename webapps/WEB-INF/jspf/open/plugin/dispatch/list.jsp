<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<div id="title"><h1>${l.l('Рассылки')}</h1></div>
	<div class="pl2 pr2">
		<c:set var="config" value="${ctxSetup.getConfig('ru.bgcrm.plugin.dispatch.Config')}"/>

		<table class="data" style="width: 100%;" id="${tableUiid}">
			<tr>
				<td width="40%">${l.l('Title')}</td>
				<td width="60%">${l.l('Description')}</td>
			</tr>
			<c:forEach var="item" items="${form.response.data.list}">
				<tr>
					<td>${item.title}</td>
					<td>${item.comment}</td>
				</tr>
			</c:forEach>
		</table>

		<html:form action="/open/plugin/dispatch/dispatch" styleClass="mt1 in-mr1">
			<input type="hidden" name="action" value="subscribe"/>
			<input type="text" name="email" placeholder="E-Mail" size="30"/>
			<button class="btn-grey" type="button" onclick="
				$$.ajax
					.post(this.form)
					.done(() =>
						alert('${l.l('На указанный адрес выслана инструкция')}')
					);
			">Подписка</button>
		</html:form>
	</div>
</div>
