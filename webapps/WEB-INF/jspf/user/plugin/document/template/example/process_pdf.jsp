<%@ page contentType="text/plain; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Example PDF document generated from a process card, tab "Documents".

How to configure:

1) In server configuration:
# example PDF document in the process card
document:pattern.103.title=Example Process PDF
document:pattern.103.scope=process
document:pattern.103.script=ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator
document:pattern.103.type=pdfForm
document:pattern.103.jsp=/WEB-INF/jspf/user/plugin/document/template/example/process_pdf.jsp
document:pattern.103.file=docpattern/example/process.pdf
document:pattern.103.documentTitle=document.pdf
document:pattern.103.result=stream,save
document:pattern.103.flattening=1

2) In process type configuration:
document:processShowDocuments=1
document:processCreateDocumentsAllowedTemplates+=,103

In the process card, on the "Documents" tab, "Example Process PDF" should appear
with the ability to both generate it "on the fly" and save the generated document.
--%>

<%--
Example PDF document generated from a process queue.

How to configure:

1) In server configuration:
# example PDF document in the process queue
document:pattern.104.scope=processQueue
document:pattern.104.script=ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator
document:pattern.104.type=pdfForm
document:pattern.104.jsp=/WEB-INF/jspf/user/plugin/document/template/example/process_pdf.jsp
document:pattern.104.file=docpattern/example/process.pdf
document:pattern.104.documentTitle=document.pdf
document:pattern.104.flattening=1

2) In process queue configuration:
# print handler
processor.{@inc:cnt}.title=Example Queue PDF Document
processor.{@cnt}.class=DefaultMarkedProcessor
processor.{@cnt}.commands=print:104

A menu item "Example Queue PDF Document" should appear in the process queue's "More" menu.
--%>

<%-- set your parameter ID --%>
<c:set var="PROCESS_PARAM_ADDRESS" value="35"/>

<%-- this is just for debugging --%>
Event: ${event}

<u:newInstance var="processDao" clazz="ru.bgcrm.dao.process.ProcessDAO">
	<u:param value="${conSlave}"/>
</u:newInstance>
<u:newInstance var="paramDao" clazz="org.bgerp.dao.param.ParamValueDAO">
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
		${link.linkObjectTitle}
	</c:forEach>
</c:set>

<%-- setting template variables --%>
${field.set('number', processId)}
${field.set('executors', u.getObjectTitles(ctxUserList, process.getExecutorIds()))}
${field.set('abonent', abonent)}
${field.set('address', address)}
${field.set('description', process.description)}