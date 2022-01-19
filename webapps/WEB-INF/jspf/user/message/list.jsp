<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- <c:set var="config" value="${ctxSetup.getConfig('ru.bgcrm.dao.message.config.MessageTypeConfig')}"/> --%>

<%@ include file="list_call_reg.jsp"%>

<c:set var="editorUiid" value="${u:uiid()}"/>

<c:set var="formUiid" value="${u:uiid()}"/>

<html:form action="/user/message" styleId="${formUiid}" styleClass="in-mr05 pr05">
	<input type="hidden" name="action" value="messageList"/>

	<c:set var="script">$$.ajax.load($('#${formUiid}'), $$.shell.$content(this));</c:set>

	<ui:combo-single
		hiddenName="typeId" value="${form.param.typeId}"
		widthTextValue="15em"
		onSelect="${script}">
		<jsp:attribute name="valuesHtml">
			<li value="0">${l.l('Все')}&nbsp;[${config.unprocessedMessagesCount}]</li>
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
		prefixText="${l.l('Обработаны')}:" widthTextValue="20px"
		onSelect="${script}">
		<jsp:attribute name="valuesHtml">
			<li value="0">${l.l('Нет')}</li>
			<li value="1">${l.l('Да')}</li>
		</jsp:attribute>
	</ui:combo-single>

	<c:if test="${form.param['processed'] eq 1}">
		<ui:date-time type="ymd" paramName="dateFrom" value="${form.param.dateFrom}" placeholder="${l.l('Дата от')}"/>
		<ui:date-time type="ymd" paramName="dateTo" value="${form.param.dateTo}" placeholder="${l.l('Дата по')}"/>
		<input type="text" name="from" value="${form.param.from}" placeholder="${l.l('Отправитель')}"/>
	</c:if>

	<ui:combo-single
		hiddenName="order" value="${form.param.order}" prefixText="${l.l('Сортировка')}:"
		widthTextValue="20px" onSelect="${script}">
		<jsp:attribute name="valuesHtml">
			<li value="1">${l.l('Обратная')}</li>
			<li value="0">${l.l('Прямая')}</li>
		</jsp:attribute>
	</ui:combo-single>

	<ui:button type="out" onclick="${script}"/>

	<%-- This reload breaks automatically open calls.
		<script>
		$(function () {
			const $messageQueue = $('#content > #message-queue');
			$messageQueue.data('onShow', function() {
				if($('#${formUiid}').length) {
					${script}
				}
			});
		});
	</script> --%>
</html:form>

<div class="mt1" style="max-height: 300px; overflow: auto;">
	<%@ include file="list_subjects.jsp"%>
</div>

<div>
	<h2>&nbsp;</h2>
	<hr/>
</div>

<div id="${editorUiid}" class="data-table">
	<%-- message is opened here --%>
</div>

<shell:title ltext="Сообщения"/>
<shell:state help="kernel/message.html"/>