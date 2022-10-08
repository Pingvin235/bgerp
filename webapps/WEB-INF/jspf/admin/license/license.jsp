<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="error" value="${license.error}"/>
<c:set var="stateUiid" value="${u:uiid()}"/>

<c:choose>
	<c:when test="${empty error}">
		<c:set var="color" value="green"/>
		<c:set var="text" value="OK"/>
	</c:when>
	<c:otherwise>
		<c:set var="color" value="red"/>
		<c:set var="text" value="${error}"/>
	</c:otherwise>
</c:choose>

<h1 class="state" id="${stateUiid}" style="color: ${color};">${text}</h1>

<div class="box p05" style="white-space: nowrap; overflow-x: auto; background-color: #e5e5e5;">
	${u:htmlEncode(license.data)}
</div>

<p:check action="org.bgerp.action.admin.LicenseAction:upload">
	<c:set var="uploadFormId" value="${u:uiid()}"/>
	<form id="${uploadFormId}" action="${form.httpRequestURI}" method="POST" enctype="multipart/form-data" name="form">
		<input type="hidden" name="action" value="upload"/>
		<input type="hidden" name="responseType" value="json"/>
		<input type="file" name="file" style="visibility: hidden; display: none;"/>
		<button type="button" class="btn-grey w100p mt1" onclick="$$.ajax.triggerUpload('${uploadFormId}');">${l.l('Загрузить файл лицензии')}</button>
	</form>
	<script>
		$$.ajax.upload('${uploadFormId}', 'lic-upload-iframe', () => {
			$$.ajax.loadContent('${form.requestUrl}', this);
		});
	</script>
</p:check>

<shell:title ltext="License"/>
<shell:state moveSelector="#${stateUiid}" help="kernel/setup.html#license"/>
