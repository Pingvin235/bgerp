<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/user/plugin/dispatch/dispatch" styleClass="center500">
	<input type="hidden" name="action" value="messageUpdate" />

	<c:set var="message" value="${form.response.data.message}" scope="page"/>

	<h2>ID</h2>
	<input type="text" name="id" style="width: 100%" value="${message.id}" disabled="disabled"/>

	<c:if test="${not empty message.createTime }">
		<h2>${l.l('Создано')}</h2>
		<h3>${tu.format( message.createTime, 'ymdhms' )}</h3>
	</c:if>

	<c:choose>
		<c:when test="${not empty message.sentTime }">
			<h2>${l.l('Отправлено')}</h2>
			<h3>${tu.format( message.sentTime, 'ymdhms' )}</h3>

			<c:set var="disabled">disabled="disabled"</c:set>
		</c:when>
		<c:otherwise>
			<h2>Готово к отправке</h2>
			<ui:combo-single hiddenName="ready" value="${message.ready}">
				<jsp:attribute name="valuesHtml">
					<li value="false">Не готово</li>
					<li value="true">Готово</li>
				</jsp:attribute>
			</ui:combo-single>
		</c:otherwise>
	</c:choose>

	<h2>Тема</h2>
	<input type="text" name="title" style="width: 100%" ${disabled} value="${message.title}"/>

	<h2>Текст</h2>
	<textarea name="text" style="width:100%; resize: none;" wrap="off" rows="20" ${disabled} id="${uiidTo}">${message.text}</textarea>

	<h2>Рассылки</h2>

	<c:choose>
		<c:when test="${empty message.sentTime}">
			<ui:select-mult list="${dispatchList}" hiddenName="dispatchId" values="${message.dispatchIds}" style="width: 100%;" showId="${true}"/>
		</c:when>
		<c:otherwise>
			${u:objectTitleList( dispatchList, message.dispatchIds )}
		</c:otherwise>
	</c:choose>

	<ui:form-ok-cancel styleClass="mt1"/>
</html:form>

<shell:state text="${l.l('Редактор')}"/>