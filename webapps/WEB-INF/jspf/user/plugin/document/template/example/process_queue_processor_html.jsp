<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Пример документа HTML, генерируемого из очереди процессов.

Как настроить:

1) В конфигурации сервера:
# пример документа HTML очереди процессов
document:pattern.100.scope=processQueue
document:pattern.100.script=ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator
document:pattern.100.type=jspHtml
document:pattern.100.jsp=/WEB-INF/jspf/user/plugin/document/template/example/process_queue_processor_html.jsp

2) В конфигурации очереди процессов:
# обработчик печати
processor.5.title=Пример документа очередь HTML
processor.5.class=DefaultMarkedProcessor
processor.5.commands=print:100
processor.5.responseType=file

В меню "Ещё" очереди процессов должен появиться пункт "Пример документа очередь HTML".
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
			Пример документа очередь HTML
		</div>
		<div>
			Событие: ${event}<br/><br/>

			<table class="data">
				<tr>
					<td>ID процесса</td>
					<td>Исполнители</td>
					<td>Адрес</td>
					<td>Услуги</td>
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
							Услуги: <%--${paramDao.getParamListWithTitles(processId, PROCESS_PARAM_LIST)} --%>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</body>
</html>