<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
Пример документа HTML, генерируемого из карточки процесса, вкладка "Документы".

Как настроить:

1) В конфигурации сервера:
# пример документа HTML в карточке процесса
document:pattern.101.title=Пример процесс HTML
document:pattern.101.scope=process
document:pattern.101.script=ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator
document:pattern.101.type=jspHtml
document:pattern.101.jsp=/WEB-INF/jspf/user/plugin/document/template/example/process_html.jsp
document:pattern.101.documentTitle=document.html
document:pattern.101.result=stream,save

2) В конфигурации типа процесса:
document:processShowDocuments=1
document:processCreateDocumentsAllowedTemplates+=,101

В карточке процессы во вкладке "Документы" должен появиться "Пример процесс HTML"
с возможностью как сгенерировать "на лету", так и сохранить сгенерированный документ.
--%>

<%-- установите ваши значения параметров --%>
<c:set var="PROCESS_PARAM_ADDRESS" value="35"/>
<c:set var="PROCESS_PARAM_LIST" value="32"/>

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="/css/style.css.jsp"/>
		<meta content="text/html; charset=UTF-8" http-equiv="content-type">
	</head>
	<body>
		<div style="text-align: center; font-weight: bold;" class="mb1">
			Пример документа процесс HTML
		</div>
		<div>
			Событие: ${event}<br/><br/>

			<u:newInstance var="processDao" clazz="ru.bgcrm.dao.process.ProcessDAO">
				<u:param value="${conSlave}"/>
			</u:newInstance>
			<u:newInstance var="paramDao" clazz="org.bgerp.dao.param.ParamValueDAO">
				<u:param value="${conSlave}"/>
			</u:newInstance>

			<c:set var="processId" value="${event.objectId}"/>
			<c:set var="process" value="${processDao.getProcess(processId)}"/>

			Исполнители: ${u:objectTitleList(ctxUserList, process.getExecutorIds())}<br/>
			Адрес:
				<c:forEach var="addr" items="${paramDao.getParamAddress(processId, PROCESS_PARAM_ADDRESS).values()}" varStatus="status">
					${addr.value}
				</c:forEach>
			<br/>
			Услуги: ${paramDao.getParamListWithTitles(processId, PROCESS_PARAM_LIST)}

			<%--
				Получение привязанного договора BGBilling.

				<c:set var="contractLink" value="${linkDao.getObjectLinksWithType(processId, 'contract%')[0]}"/>
				<c:set var="contractId" value="${contractLink.linkObjectId}"/>
				<c:set var="contractTitle" value="${contractLink.linkObjectTitle}"/>

				Пример выбора услуг в модуле Inet.
				  bgbilling - идентификатор биллинга в конфигурации BGERP
				  18 - код экземпляра модуля Inet.

				<u:newInstance var="inetDao" clazz="ru.bgcrm.plugin.bgbilling.proto.dao.InetDAO">
					<u:param value="${ctxUser}"/>
					<u:param value="bgbilling"/>
					<u:param value="18"/>
				</u:newInstance>
				<c:set var="serviceList" value="${inetDao.getServiceList(contractId)}"/>

				Итерация по сервисам договора, вывод только сервисов с пустой датой закрытия и типами 1 и 2.
				<c:forEach var="service" items="${serviceList}">
					<c:if test="${empty service.dateTo and (service.typeId eq 2 or service.typeId eq 1}">
						<c:set var="typeTitle" value="${service.typeTitle}"/>
						<c:set var="deviceTitle" value="${service.deviceTitle}"/>
						<c:set var="deviceStateTitle" value="${service.deviceStateTitle}"/>
						<c:set var="comment" value="${service.comment}"/>

						d ${service} d ${typeTitle} d ${deviceTitle} d ${deviceStateTitle} d ${comment}<br>
					</c:if>
				</c:forEach>
			--%>
		</div>
	</body>
</html>