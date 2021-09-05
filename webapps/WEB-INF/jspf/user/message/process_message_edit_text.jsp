<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div>
	<h2>${l.l('Тема')}</h2>
	<input type="text" name="subject" style="width: 100%;" value="${message.subject}"/>
</div>
<div>
	<h2>${l.l('Сообщение')}</h2>
	<textarea rows="20" style="width: 100%; resize: vertical;" name="text" class="tabsupport">${message.text}</textarea>
	<span class="hint">${l.l('Вы можете использовать #ID для ссылок на другие процессы, подобные записи будут автоматически преобразованы в ссылки открытия карточек')}.</span>
</div>
