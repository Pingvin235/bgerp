<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${typeEmail}">
	<ui:combo-single hiddenName="notification" prefixText="${l.l('Уведомление')}:" styleClass="mr1">
		<jsp:attribute name="valuesHtml">
			<li value="0">${l.l('Нет')}</li>
			<li value="1">${l.l('Да')}</li>
		</jsp:attribute>
	</ui:combo-single>
</c:if>

<c:if test="${not empty messageType.contactSaver}">
	<ui:combo-single hiddenName="contactSaveMode" prefixText="${l.l('Контакт')}" styleClass="mr1">
		<jsp:attribute name="valuesHtml">
			<c:forEach var="item" items="${messageType.contactSaver.saveModeList}">
				<li value="${item.id}">${item.getTitle(l)}</li>
			</c:forEach>
		</jsp:attribute>
	</ui:combo-single>
</c:if>

<% out.flush(); %>