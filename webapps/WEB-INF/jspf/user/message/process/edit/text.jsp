<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div style="display: flex;">
	<div>
		<c:set var="templates" value="${templateConfig.templates.values()}"/>
		<c:if test="${not empty templates}">
			<h2>${l.l('Template')}</h2>
			<ui:combo-single list="${templates}" onSelect="$$.message.templateLoad(this, '${l.l('Load template?')}')" widthTextValue="10em"/>
		</c:if>
	</div>
	<div class="w100p ${not empty templates ? 'pl1' : ''}">
		<h2>${l.l('Тема')}</h2>
		<input type="text" name="subject" style="width: 100%;" value="${message.subject}"/>
	</div>
</div>
<div>
	<h2>${l.l('Сообщение')}</h2>
	<textarea rows="20" style="width: 100%; resize: vertical;" name="text" class="tabsupport">${message.text}</textarea>
	<span class="hint">${l.l('message.edit.hint')}</span>
</div>
