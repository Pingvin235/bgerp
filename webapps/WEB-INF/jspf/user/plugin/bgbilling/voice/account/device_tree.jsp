<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mt1 mb1">
	<h2>Устройство</h2>

	<ui:tree-single rootNode="${frd.rootDevice}" name="deviceIdSelect" nameTitle="deviceTitleSelect"
		value="${form.param.deviceId}" style="height: 20em; overflow: auto;"/>

	<c:set var="closeCode">$(this.form).find('#needDevice').toggle(); $(this.form).find('#deviceEditor').toggle();</c:set>

	<button type="button" class="btn-white mr1" onclick="$$.bgbilling.voice.setDevice(this.form); ${closeCode}">OK</button>
	<ui:button type="cancel" onclick="${closeCode}"/>
</div>