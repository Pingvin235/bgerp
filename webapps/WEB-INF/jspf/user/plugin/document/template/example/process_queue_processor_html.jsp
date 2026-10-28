<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Example HTML document generated from a process queue.

How to configure:

1) In server configuration:
# example HTML document for a process queue
document:pattern.100.scope=processQueue
document:pattern.100.script=ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator
document:pattern.100.type=jspHtml
document:pattern.100.jsp=/WEB-INF/jspf/user/plugin/document/template/example/process_queue_processor_html.jsp

2) In process queue configuration:
# print handler
processor.5.title=Example Queue HTML Document
processor.5.class=DefaultMarkedProcessor
processor.5.commands=print:100

A menu item "Example Queue HTML Document" should appear in the process queue's "More" menu.
 --%>

<%-- set your param IDs in configuration --%>
<c:set var="PROCESS_PARAM_ADDRESS" value="${ctxSetup.getInt('document:param.process.address')}"/>
<c:set var="PROCESS_PARAM_LIST" value="32"/>

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="/css/css.jsp"/>
	</head>
	<body>
		<div style="text-align: center; font-weight: bold;" class="mb1">
			Example Queue HTML Document
		</div>
		<div>
			Event: ${event}<br/><br/>

			<table class="data">
				<tr>
					<td>Process ID</td>
					<td>Executors</td>
					<td>Address</td>
					<td>Services</td>
				</tr>

				<u:newInstance var="processDao" clazz="ru.bgcrm.dao.process.ProcessDAO">
					<u:param value="${conSlave}"/>
				</u:newInstance>
				<u:newInstance var="paramDao" clazz="org.bgerp.dao.param.ParamValueDAO">
					<u:param value="${conSlave}"/>
				</u:newInstance>

				<c:forEach var="processId" items="${event.getObjectIds()}">
					<c:set var="process" value="${processDao.getProcess(processId)}"/>

					<tr>
						<td>${processId}</td>
						<td>${u.getObjectTitles(ctxUserList, process.getExecutorIds())}</td>
						<td>
							<c:forEach var="addr" items="${paramDao.getParamAddress(processId, PROCESS_PARAM_ADDRESS).values()}" varStatus="status">
								${addr.value}
							</c:forEach>
						</td>
						<td>
							Services: <%--${paramDao.getParamListWithTitles(processId, PROCESS_PARAM_LIST)} --%>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</body>
</html>