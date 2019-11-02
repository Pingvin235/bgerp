<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty stepData.commonContract}">

	<div style="float:left;overflow:auto;">
		<table class="data">
		<tr>
			<td colspan="2">Единый договор</td>
		</tr>
		<tr>
			<td>Пароль статистики</td>
			<td>${stepData.commonContract.password}</td>
		</tr>
		</table>
	</div>
</c:if>

<c:if test="${not empty stepData.vpnLogin}">
	<div style="overflow:auto;">
		<table class="data">
		<tr>
			<td colspan="2">Модуль VPN</td>
		</tr>
		<tr>
			<td>Логин</td>
			<td>${stepData.vpnLogin}</td>
		</tr>
		<tr>
			<td>Пароль</td>
			<td>${stepData.vpnPassword}</td>
		</tr>
	</table>
</div>
</c:if>
