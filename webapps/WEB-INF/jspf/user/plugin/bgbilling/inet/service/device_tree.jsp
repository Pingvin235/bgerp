<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<%-- in-inline-block  --%>
<div id="${uiid}" class="mt1 mb1">
	<%-- <div style="width: 50%;"> --%>
		<h2>Устройства</h2>

		<ui:tree-single rootNode="${form.response.data.rootDevice}" hiddenName="deviceIdSelect" hiddenNameTitle="deviceTitleSelect" value="${form.param.deviceId}"
			style="height: 30em; overflow: auto;" />
		<button class="btn-white mr1" type="button" onclick="$$.bgbilling.inet.setDevice(this.form)">OK</button>
		<button class="btn-white" type="button" onclick="$('#${uiid}').parent().text('')">Отмена</button>
</div>