<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>${l.l('Вложения')}</h2>
<div class="upload-list in-mb05-all">
	<%-- already stored attachments --%>
	<c:forEach var="item" items="${message.attachList}">
		<div>
			<input type="hidden" name="fileId" value="${item.id}"/>
			<ui:button styleClass="btn-small mr05" type="del" onclick="$(this.parentNode).remove()"/>
			<ui:file-link file="${item}"/>
		</div>
	</c:forEach>

	<%-- here is generated a list of newly uploaded attachments --%>
</div>
<div>
	<ui:combo-single name="addFileId" style="width: 20em;">
		<jsp:attribute name="valuesHtml">
			<li value="0">${l.l('Выбрать файл')}</li>
			<c:forEach var="file" items="${files}">
				<li value="${file.id}">${file.title}</li>
			</c:forEach>
			<c:forEach var="file" items="${announcedFiles}">
				<li value="${file.id}">${file.title}</li>
			</c:forEach>
		</jsp:attribute>
	</ui:combo-single>

	<ui:button type="add" styleClass="ml05" onclick="$$.ui.upload.add(this.form, '${uploadFormId}');"/>
</div>

