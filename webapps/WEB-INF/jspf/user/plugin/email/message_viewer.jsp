<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>Email</h2>

<b>${l.l('Тема')}:</b> ${message.subject}<br/>
<b>${l.l('От')}:</b> <a href="mailto:${message.from}">${message.from}</a><br/>
<b>${l.l('Текст')}:</b><br/>
	<ui:text-prepare text="${message.text}"/>
<c:if test="${not empty message.attachList}">
	<br/><br/><b>${l.l('Вложения')}:</b><br/>

	<c:forEach var="item" items="${message.attachList}">
		<c:choose>
			<c:when test="${message.id gt 0}">
				<ui:file-link file="${item}"/>
			</c:when>
			<c:otherwise>
				<c:url var="url" value="/user/plugin/email/email.do">
					<c:param name="method" value="getAttach"/>
					<c:param name="typeId" value="${message.typeId}"/>
					<c:param name="messageId" value="${form.param.messageId}"/>
					<c:param name="title" value="${item.title}"/>
				</c:url>
				<a href="${url}">${item.title}</a>
			</c:otherwise>
		</c:choose>
		<br/>
	</c:forEach>
</c:if>