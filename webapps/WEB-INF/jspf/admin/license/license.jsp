<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="box p05" style="white-space: nowrap; overflow-x: auto; background-color: #e5e5e5;">
	${u:htmlEncode(license.data)}
</div>

<p:check action="org.bgerp.action.admin.LicenseAction:upload">
	<form action="${form.httpRequestURI}" method="POST" enctype="multipart/form-data">
		<input type="hidden" name="method" value="upload"/>
		<input type="hidden" name="responseType" value="json"/>
		<input type="file" name="file" style="visibility: hidden; display: none;"/>
		<button type="button" class="btn-grey w100p mt1" onclick="$$.ajax.fileUpload(this.form).done(() => {
			$$.ajax.loadContent('${form.requestUrl}', this);
		})">${l.l('Загрузить файл лицензии')}</button>
	</form>
</p:check>

<shell:title text="${l.l('License')}"/>
<shell:state error="${license.error}" help="kernel/setup.html#license"/>
