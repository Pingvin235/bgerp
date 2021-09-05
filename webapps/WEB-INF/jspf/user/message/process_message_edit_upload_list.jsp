<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>${l.l('Вложения')}</h2>
<div id="${uploadListId}" class="in-mb05-all">
	<%-- already stored attachments --%>
	<c:forEach var="item" items="${message.attachList}">
		<c:url var="url" value="/user/file.do">
			<c:param name="id" value="${item.id}"/>
			<c:param name="title" value="${item.title}"/>
			<c:param name="secret" value="${item.secret}"/>
		</c:url>

		<div>
			<input type="hidden" name="fileId" value="${item.id}"/>
			<ui:button styleClass="btn-small mr1" type="del" onclick="$(this.parentNode).remove()"/>
			<a href="${url}">${item.title}</a>
		</div>
	</c:forEach>

	<%-- here is generated a list of newly uploaded attachments --%>
</div>
<ui:button type="add" styleClass="btn-small" onclick="$$.ajax.triggerUpload('${uploadFormId}');"/>
