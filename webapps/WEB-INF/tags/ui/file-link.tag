<%@ tag body-content="empty" pageEncoding="UTF-8" description="Link for downloading a file"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="file" description="Process object" type="org.bgerp.model.file.FileData" required="true"%>

<c:url var="url" value="/user/file.do">
	<c:param name="id" value="${file.id}"/>
	<c:param name="title" value="${file.title}"/>
	<c:param name="secret" value="${file.secret}"/>
</c:url>
<a href="${url}" class="preview">${file.title}</a>
