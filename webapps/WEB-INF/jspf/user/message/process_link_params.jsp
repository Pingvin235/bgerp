<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${typeEmail}">
	<u:sc>
		<c:set var="valuesHtml">
			<li value="0">Нет</li>
			<li value="1">Да</li>
		</c:set>
		<c:set var="hiddenName" value="notification"/>
		<c:set var="value" value=""/>
		<c:set var="prefixText" value="Уведомление:"/>
		<c:set var="styleClass" value="mr1"/>							
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
	</u:sc>
</c:if>

<c:if test="${not empty messageType.contactSaver}">
	<u:sc>
		<c:set var="list" value="${messageType.contactSaver.saveModeList}"/>
		<c:set var="hiddenName" value="contactSaveMode"/>
		<c:set var="value" value=""/>
		<c:set var="prefixText" value="Контакт:"/>
		<c:set var="styleClass" value="mr1"/>							
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
	</u:sc>
</c:if>

<% out.flush(); %>