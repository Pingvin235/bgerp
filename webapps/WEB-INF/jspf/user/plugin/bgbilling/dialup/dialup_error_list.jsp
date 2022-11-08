<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table width="100%" class="hdata">
	<tr class="header">
		<td colspan="6">Ошибки логина ${form.param.login} за последние ${form.param.errorDays} суток.

			<button style="border:none; background:transparent; cursor: pointer; text-decoration:underline;"
				onclick="$('#${form.param.uiid}sessionErrorList').empty();">[закрыть]</button>

			<form action="/user/plugin/bgbilling/proto/dialup.do">
				<input type="hidden" name="action" value="errorList"/>
				<input type="hidden" name="billingId" value="${form.param.billingId}"/>
				<input type="hidden" name="contractId" value="${form.param.contractId}"/>
				<input type="hidden" name="moduleId" value="${form.param.moduleId}"/>
				<input type="hidden" name="loginId" value="${form.param.loginId}"/>
				<input type="hidden" name="login" value="${form.param.login}"/>
				<input type="hidden" name="uiid" value="${form.param.uiid}"/>

				Показать ошибки за:

				<select name="errorDays" onchange="$$.ajax.load(this.form, $('#${form.param.uiid}sessionErrorList') ); scrollToElementById('${form.param.uiid}sessionErrorList');">
				  <option disabled selected style='display:none;'>...</option>
				  <option>1</option>
				  <option>2</option>
				  <option>3</option>
				</select>

				суток.
			</form>
		</td>
	</tr>
		<tr class="header">
		<td>Время</td>
		<td>NAS</td>
		<td>Логин</td>
		<td>Ошибка</td>
		<td>RADIUS</td>
	</tr>

	<c:forEach var="dialUpError" items="${form.response.data.errorList}">
			<tr align="center">
				<td>${dialUpError['date']}</td>
				<td>${dialUpError['nas']}</td>
				<td>${dialUpError['login']}</td>
				<td>${dialUpError['error']}</td>

				<c:url var="radiusLogUrl" value="plugin/bgbilling/proto/dialup.do">
					<c:param name="action" value="radiusLog" />
					<c:param name="splitter" value="</br>" />
					<c:param name="moduleId" value="${form.param.moduleId}" />
					<c:param name="billingId" value="${form.param.billingId}" />
					<c:param name="sessionStart" value="${dialUpError['date']}" />
					<c:param name="sessionId" value="${dialUpError['id']}" />
				</c:url>
				<td>
					<input type="button" style="width: 100%" value="Show Log" onclick="if ($('#${form.param.contractId}-${dialUpError['id']}-radiusLog').children().size()<=0 ) { $$.ajax.load('${radiusLogUrl}', $('#${form.param.contractId}-${dialUpError['id']}-radiusLog')  ); } else { $('#${form.param.contractId}-${dialUpError['id']}-radiusLog').toggle(); }" />
				</td>
			</tr>
			<tr style="border-spacing:0px;">
				<td colspan="5" style="padding: 0em;">
					<div id="${form.param.contractId}-${dialUpError['id']}-radiusLog">
					</div>
				</td>
			</tr>
	</c:forEach>
</table>