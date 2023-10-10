<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- the form is placed separately to be extracted from process message editors --%>
<c:set var="uploadFormId" value="${u:uiid()}" scope="request"/>

<form id="${uploadFormId}" action="/user/file.do" method="POST" enctype="multipart/form-data" style="position: absolute; top: -100px;">
	<input type="hidden" name="action" value="temporaryUpload"/>
	<input type="hidden" name="responseType" value="json"/>
	<input type="file" name="file" multiple="true"/>
</form>
