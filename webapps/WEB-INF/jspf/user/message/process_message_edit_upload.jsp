<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uploadListId" value="${u:uiid()}" scope="request"/>
<c:set var="uploadFormId" value="${u:uiid()}" scope="request"/>

<form id="${uploadFormId}" action="/user/file.do" method="POST" enctype="multipart/form-data" name="form" style="position: absolute; top: -100px;">
	<input type="hidden" name="action" value="temporaryUpload"/>
	<input type="hidden" name="responseType" value="json"/>
	<input type="file" name="file" />
</form>

<script>
	$$.ajax.upload('${uploadFormId}', 'message-attach-upload', function (response) {
		const fileId = response.data.file.id;
		const fileTitle = response.data.file.title;

		const deleteCode = "$$.ajax.post('/user/file.do?action=temporaryDelete&id=" + fileId + "').done(() => {$(this.parentNode).remove()})";

		$('#${uploadFormId}').parent().find('>form:visible').find('#${uploadListId}').append(
			"<div>" +
				"<input type=\"hidden\" name=\"tmpFileId\" value=\""+ fileId + "\"/>" +
				"<button class=\"btn-white btn-small mr1 icon\" type=\"button\" onclick=\"" + deleteCode + "\"><i class=\"ti-trash\"></i></button> " + fileTitle +
			"</div>"
		);
	});
</script>

