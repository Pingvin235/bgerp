<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="list_call_reg.jsp"%>

<c:set var="editorUiid" value="${u:uiid()}"/>

<c:set var="formUiid" value="${u:uiid()}"/>

<html:form action="/user/message" styleId="${formUiid}" styleClass="in-mr05 pr05">
	<input type="hidden" name="method" value="messageList"/>

	<%-- used also in list_subjects.jsp --%>
	<c:set var="script">$$.ajax.loadContent($('#${formUiid}'));</c:set>

	<ui:combo-single
		hiddenName="typeId" value="${form.param.typeId}"
		widthTextValue="15em"
		onSelect="${script}">
		<jsp:attribute name="valuesHtml">
			<li value="0">${l.l('All')}&nbsp;[${config.unprocessedMessagesCount}]</li>
			<c:forEach var="item" items="${typeMap}">
				<li value="${item.key}">
					${item.value.title}
					<c:set var="count" value="${item.value.unprocessedMessagesCount}"/>
					<c:if test="${not empty count}">&nbsp;[${count}]</c:if>
				</li>
			</c:forEach>
		</jsp:attribute>
	</ui:combo-single>

	<ui:combo-single
		hiddenName="processed" value="${form.param.processed}"
		prefixText="${l.l('Обработаны')}:" widthTextValue="3em"
		onSelect="${script}">
		<jsp:attribute name="valuesHtml">
			<li value="0">${l.l('No')}</li>
			<li value="1">${l.l('Yes')}</li>
		</jsp:attribute>
	</ui:combo-single>

	<c:if test="${form.param.processed eq 1}">
		<ui:combo-single
			hiddenName="read" value="${form.param.read}"
			prefixText="${l.l('Read')}:" widthTextValue="3em"
			onSelect="${script}">
			<jsp:attribute name="valuesHtml">
				<li value="">${l.l('All')}</li>
				<li value="0">${l.l('No')}</li>
				<li value="1">${l.l('Yes')}</li>
			</jsp:attribute>
		</ui:combo-single>
		<ui:date-time type="ymd" name="dateFrom" value="${form.param.dateFrom}" placeholder="${l.l('Дата от')}"/>
		<ui:date-time type="ymd" name="dateTo" value="${form.param.dateTo}" placeholder="${l.l('Дата по')}"/>
		<input type="text" name="from" value="${form.param.from}" placeholder="${l.l('Отправитель')}"/>
	</c:if>

	<ui:combo-single
		hiddenName="order" value="${form.param.order}" prefixText="${l.l('Сортировка')}:"
		widthTextValue="7em" onSelect="${script}">
		<jsp:attribute name="valuesHtml">
			<li value="1">${l.l('Обратная')}</li>
			<li value="0">${l.l('Прямая')}</li>
		</jsp:attribute>
	</ui:combo-single>

	<ui:button type="out" onclick="${script}"/>
</html:form>

<div class="mt1" style="height: 300px; max-height: 300px; overflow: auto;">
	<%@ include file="list_subjects.jsp"%>
</div>

<div>
	<h2>&nbsp;</h2>
	<div class="separator"/>
</div>

<div id="${editorUiid}" class="data-table">
	<%-- message is opened here --%>
</div>

<shell:title text="${l.l('Сообщения')}"/>
<shell:state help="kernel/message.html"/>