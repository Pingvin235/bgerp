<%@ page contentType="text/plain; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Пример документа PDF, генерируемого из карточки процесса, вкладка "Документы".

Как настроить:

1) В конфигурации сервера:
# пример документа PDF в карточке процесса
document:pattern.103.title=Пример процесс PDF
document:pattern.103.scope=process
document:pattern.103.script=ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator
document:pattern.103.type=pdfForm
document:pattern.103.jsp=/WEB-INF/jspf/user/plugin/document/template/example/process_pdf.jsp
document:pattern.103.file=docpattern/example/process.pdf
document:pattern.103.documentTitle=document.pdf
document:pattern.103.result=stream,save
document:pattern.103.flattening=1

2) В конфигурации типа процесса:
document:processShowDocuments=1
document:processCreateDocumentsAllowedTemplates+=,103

В карточке процессы во вкладке "Документы" должен появиться "Пример процесс PDF"
с возможностью как сгенерировать "на лету", так и сохранть сгенерированный документ.
--%>

<%--
Пример документа PDF, генерируемого из очереди процессов.

Как настроить:

1) В конфигурации сервера:
# пример документа PDF в очереди процессов
document:pattern.104.scope=processQueue
document:pattern.104.script=ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator
document:pattern.104.type=pdfForm
document:pattern.104.jsp=/WEB-INF/jspf/user/plugin/document/template/example/process_pdf.jsp
document:pattern.104.file=docpattern/example/process.pdf
document:pattern.104.documentTitle=document.pdf
document:pattern.104.flattening=1

2) В конфигурации очереди процессов:
# обработчик печати
processor.6.title=Пример документа очередь PDF
processor.6.class=DefaultMarkedProcessor
processor.6.commands=print:104
processor.6.responseType=file

В меню "Ещё" очереди процессов должен появиться пункт "Пример документа очередь PDF".
--%>

<%-- установите ваши значения параметров --%>
<c:set var="PROCESS_PARAM_ADDRESS" value="35"/>

<%-- это просто отладка --%>
Событие: ${event}

<u:newInstance var="processDao" clazz="ru.bgcrm.dao.process.ProcessDAO">
	<u:param value="${conSlave}"/>
</u:newInstance>
<u:newInstance var="paramDao" clazz="ru.bgcrm.dao.ParamValueDAO">
	<u:param value="${conSlave}"/>
</u:newInstance>
<u:newInstance var="linkDao" clazz="ru.bgcrm.dao.process.ProcessLinkDAO">
	<u:param value="${conSlave}"/>
</u:newInstance>

<c:set var="processId" value="${event.objectId}"/>
<c:set var="process" value="${processDao.getProcess(processId)}"/>

<c:set var="address">
	<c:forEach var="addr" items="${paramDao.getParamAddress(processId, PROCESS_PARAM_ADDRESS).values()}" varStatus="status">
		${addr.value}
	</c:forEach>
</c:set>

<c:set var="abonent">
	<c:forEach var="link" items="${linkDao.getObjectLinksWithType(processId, 'customer')}">
		${link.linkedObjectTitle}
	</c:forEach>
</c:set>

<%-- установка переменных для шаблона --%>
${field.set('number', processId)}
${field.set('executors', u:objectTitleList(ctxUserList, process.getExecutorIds()))}
${field.set('abonent', abonent)}
${field.set('address', address)}
${field.set('description', process.description)}