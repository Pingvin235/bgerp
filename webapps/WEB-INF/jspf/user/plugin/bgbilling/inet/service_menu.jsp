<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="baseUrl" value="${form.httpRequestURI}">
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="id" value="${form.id}"/>
	<c:param name="deviceId" value="${form.param.deviceId}"/>
</c:url>

<c:set var="reload" value="$$.ajax.load('${form.returnUrl}', document.getElementById('${form.returnChildUiid}').parentElement)"/>

<c:url var="url" value="${baseUrl}">
	<c:param name="method" value="serviceStateModify"/>
	<c:param name="state" value="1"/>
</c:url>
<li><a href="#" onclick="
	if (confirm('Вы уверены, что хотите включить сервис на устройстве?'))
		$$.ajax.post('${url}').done(() => ${reload});
	return false;
">Включить (отладка)</a></li>

<c:url var="url" value="${baseUrl}">
	<c:param name="method" value="serviceStateModify"/>
	<c:param name="state" value="0"/>
</c:url>
<li><a href="#" onclick="
	if (confirm('Вы уверены, что хотите отключить сервис на устройстве?'))
		$$.ajax.post('${url}').done(() => ${reload});
	return false;
">Отключить (отладка)</a></li>

<%--
<li confirm="Вы уверены, что хотите синхронизировать сервис на устройстве?" command="action=serviceStateModify&state=1"><a href="#" onclick="return false;">Синхронизировать (отладка)</a></li>
--%>

<c:set var="uiidDialog" value="${u:uiid()}"/>
<div id="${uiidDialog}" style="display:none;"></div>

<c:forEach var="item" items="${frd.deviceMethods}">
	<c:url var="url" value="${baseUrl}">
		<c:param name="method" value="serviceDeviceManage"/>
		<c:param name="operation" value="${item.method}"/>
	</c:url>
	<li><a onclick="
		$$.ajax
			.post('${url}')
			.done((result) => {
				$$.bgbilling.inet.deviceManageResponse('${uiidDialog}', '${item.title}', result.data.response)
			});
		return false;"
	>${item.title}</a></li>
</c:forEach>