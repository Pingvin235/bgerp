<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>E-Mail</h2>

<b>${l.l('Тема')}:</b> ${message.subject}<br/>
<b>${l.l('От')}:</b> <a href="mailto:${message.from}">${message.from}</a><br/>
<b>${l.l('Текст')}:</b><br/>
	<ui:text-prepare text="${message.text}"/>
<c:if test="${not empty message.attachList}">
	<br/><br/><b>${l.l('Вложения (можно загрузить только после привязки процесса)')}:</b><br/>

	<c:forEach var="item" items="${message.attachList}">
		<c:choose>
			<c:when test="${message.id gt 0}">
				<c:url var="url" value="/user/file.do">
					<c:param name="id" value="${item.id}"/>
					<c:param name="title" value="${item.title}"/>
					<c:param name="secret" value="${item.secret}"/>
				</c:url>
				<a href="${url}">${item.title}</a><br/>
			</c:when>
			<c:otherwise>
				${item.title}
			</c:otherwise>
		</c:choose>
	</c:forEach>
</c:if>