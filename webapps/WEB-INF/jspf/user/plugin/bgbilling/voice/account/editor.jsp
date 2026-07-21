<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="account" value="${frd.account}"/>
<c:set var="types" value="${frd.types}"/>

<h1>Редактор</h1>

<html:form action="${form.requestURI}">
	<input type="hidden" name="method" value="accountUpdate"/>
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="id"/>

	<div class="in-table-cell in-pl1">
		<div class="w100p">
			<h2>Тип</h2>

			<c:set var="typeSelectUiid" value="${u:uiid()}"/>

			<c:set var="onSelectCode">
				$$.bgbilling.voice.accountTypeChanged('${typeSelectUiid}', ${ui.json(types)});
			</c:set>

			<ui:select-single id="${typeSelectUiid}" name="typeId" value="${account.typeId}" list="${types}"
					onSelect="${onSelectCode}" inputAttrs="${account.id gt 0 ? 'disabled' : ''}"
					style="width: 100%;"/>

			<script>
				$(function () {
					${onSelectCode}
				})
			</script>
		</div>
		<div class="nowrap">
			<h2>Период</h2>
			c <ui:date-time name="dateFrom" value="${tu.format(account.dateFrom, 'ymd')}"/>
			по <ui:date-time name="dateTo" value="${tu.format(account.dateTo, 'ymd')}"/>
		</div>
		<div>
			<h2>Статус</h2>
			<ui:combo-single name="status" value="${account.status.code}" widthTextValue="12em">
				<jsp:attribute name="valuesHtml">
					<li value="0">Включен</li>
					<li value="1">Выключен</li>
					<li value="2">Заблокирован</li>
				</jsp:attribute>
			</ui:combo-single>
		</div>
		<div>
			<h2>Кол-во сессий</h2>
			<ui:input-decimal name="sessions" value="${account.sessionCountLimit}" digits="0" style="text-align: center;"/>
		</div>
	</div>

	<table class="data mt1" style="display: none;">
		<tr>
			<td>Параметр</td>
			<td>Значение</td>
		</tr>
		<tr id="needDevice">
			<td>Устройство</td>
			<td>
				<input type="hidden" name="deviceId" value="${account.deviceId}"/>
				<a href="#" onclick="$$.bgbilling.voice.devices(this); return false;">${u.maskEmpty(account.deviceTitle, 'не выбрано')}</a>
			</td>
		</tr>
		<tr id="deviceEditor" style="display: none;"><td colspan="2"></td></tr>
		<tr id="needLogin">
			<td>Логин</td>
			<td>
				<input type="text" name="login" value="${account.login}" class="w100p"/>
			</td>
		</tr>
		<tr id="checkPassword">
			<td>Пароль</td>
			<td>
				<div style="display: flex;" class="nowrap">
					<input type="password" name="pswd" value="*******" class="w100p mr1"/>
					<ui:toggle name="generatePassword" prefixText="авто"/>
				</div>
			</td>
		</tr>
		<tr id="needPhone">
			<td>Номер телефона</td>
			<td>
				<div style="display: flex;">
					<input type="text" name="number" value="${account.number}" disabled="disabled" class="w100p mr1"/>
					<button type="button" class="btn-white" onclick="$$.bgbilling.voice.categories(this)">&lt;&lt;&lt;</button>
				</div>
			</td>
		</tr>
		<tr id="phoneEditor" style="display: none;"><td colspan="2"></td></tr>
	</table>

	<div class="mt1">
		<h2>Комментарий</h2>
		<textarea style="width: 100%; height: 5em; resize: vertical;" name="comment">${account.comment}</textarea>
	</div>

	<div class="mt1">
		<ui:form-ok-cancel/>
	</div>
</html:form>