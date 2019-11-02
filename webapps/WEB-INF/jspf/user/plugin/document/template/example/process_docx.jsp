<%@ page contentType="text/plain; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Пример документа DOCX, генерируемого из карточки процесса, вкладка "Документы". 

Как настроить:

1) В конфигурации сервера:
# пример документа DOCX в карточке процесса
document:pattern.102.title=Пример процесс DOCX
document:pattern.102.scope=process
document:pattern.102.script=ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator
document:pattern.102.type=docxForm
document:pattern.102.jsp=/WEB-INF/jspf/user/plugin/document/template/example/process_docx.jsp
document:pattern.102.file=docpattern/example/process.docx
document:pattern.102.documentTitle=document.docx
document:pattern.102.result=stream,save
document:pattern.102.flattening=1

2) В конфигурации типа процесса:
document:processShowDocuments=1
document:processCreateDocumentsAllowedTemplates+=,102

В карточке процессы во вкладке "Документы" должен появиться "Пример процесс DOCX" 
с возможностью как сгенерировать "на лету", так и сохранть сгенерированный документ. 
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

<c:set var="processId" value="${event.objectId}"/>
<c:set var="process" value="${processDao.getProcess(processId)}"/>
			
<c:set var="element">
	<c:forEach var="addr" items="${paramDao.getParamAddress(processId, PROCESS_PARAM_ADDRESS).values()}" varStatus="status">
		${addr.value}
	</c:forEach>
</c:set>

<%-- установка переменных для шаблона --%>
${field.set('cardNumber', processId)}
${field.set('element', element)}
${field.set('workPurpose', process.description)}