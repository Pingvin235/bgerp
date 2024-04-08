<%@page import="ru.bgcrm.util.Utils"%>
<%@page import="org.apache.commons.collections.CollectionUtils"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Collections"%>
<%@page import="ru.bgcrm.model.process.Process"%>
<%@page import="ru.bgcrm.plugin.document.event.DocumentGenerateEvent"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="ru.bgcrm.dao.process.ProcessDAO"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="head.jsp"%>

	<%-- сортировка процессов по исполнителю --%>
	<%
		DocumentGenerateEvent event = (DocumentGenerateEvent) request.getAttribute("event");
		ProcessDAO processDao = (ProcessDAO) pageContext.getAttribute("processDao");

		List<Process> processList = new ArrayList<Process>(event.getObjectIds().size());
		pageContext.setAttribute("processList", processList);

		for (Integer processId : event.getObjectIds())
		    processList.add(processDao.getProcess(processId));

		Collections.sort(processList, new Comparator<Process> () {
		    @Override
		    public int compare(Process p1, Process p2) {
		        return Utils.toString(p1.getExecutorIds()).compareTo(Utils.toString(p2.getExecutorIds()));
		    }
		});
	%>

	<table style="width: 100%;">
       <tr>
            <td>
                ${tu.format(curdate, 'ymd')} (Всего ${processList.size()} заявок)
            </td>
            <td style="text-align: right">
                ${ctxUser.title}
            </td>
        </tr>
    </table>

	<c:forEach var="process" items="${processList}" varStatus="status">
    	<u:sc>
	    	<c:set var="contractLink" value="${u:getFirst(processLinkDao.getObjectLinksWithType(process.id, 'contract%'))}"/>

	    	<c:if test="${not empty contractLink}">
		    	<u:newInstance var="billingDao" clazz="ru.bgcrm.plugin.bgbilling.proto.dao.ContractDAO">
					<u:param value="${ctxUser}"/>
					<u:param value="${su.substringAfter(contractLink.linkObjectType, ':')}"/>
				</u:newInstance>
				<c:set var="contractInfo" value="${billingDao.getContractInfo(contractLink.linkObjectId)}"/>
			</c:if>

			<%-- разрыв страницы, если начался новый исполнитель --%>
			<c:if test="${not empty executors and executors ne process.executorIds}">
				<div style="page-break-before: always; padding-top: 2em;"></div>
			</c:if>

	        <table style="width: 100%;">
	            <tr>
	                <td width="20%"><b>Номер заявки</b></td>
	                <td width="15%">${process.id}</td>
	                <td width="15%" style="font-style: italic">Дата выдачи</td>
	                <td width="15%">${tu.format(curdate, 'ymd')}</td>
	                <td width="15%" style="font-style: italic">Выдал</td>
	                <td width="20%">${ctxUser.title}</td>
	            </tr>
	            <tr>
	                <td style="font-style: italic">Вид работ</td>
	                <td colspan="5">${ctxProcessTypeMap[process.typeId].title}</td>
	            </tr>
	            <tr>
	                <td rowspan="3" style="font-style: italic">Клиент</td>
	                <td colspan="5">${contractInfo.comment}<br/>ТП: ${u:toString(contractInfo.getTariffList())}</td>
	            </tr>
	            <tr>
	                <td colspan="2" style="font-style: italic">Контакты</td>
	                <td colspan="3">${paramDao.getParamPhone(process.id, PROCESS_PARAM_PHONE)}</td>
	            </tr>
	            <tr>
	                <td style="font-style: italic">№ дог / логин</td>
	                <td>${contractInfo.title}</td>
	                <td style="font-style: italic">Пароль</td>
	                <td>${contractInfo.statisticPassword}</td>
	                <td>Баланс: ${contractInfo.balanceOut}</td>
	            </tr>
	            <tr>
	            	<c:set var="addr" value="${u:getFirst(paramDao.getParamAddressExt(process.id, PROCESS_PARAM_ADDRESS, true).values())}"/>

	                <td rowspan="2" style="font-style: italic">Адрес</td>
	                <td rowspan="2" colspan="2">${addr.value}</td>
	                <td style="font-style: italic">квартира</td>
	                <td style="font-style: italic">подъезд</td>
	                <td style="font-style: italic">этаж</td>
	            </tr>
	            <tr>
	                <td>${addr.flat}&nbsp;</td>
	                <td>${addr.pod}</td>
	                <td>${addr.floor}</td>
	            </tr>
	            <tr>
	                <td>Ответственный</td>
	                <td colspan="2">${u.getObjectTitles(ctxUserGroupList, process.getGroupIds())}</td>
	                <td>Время заявки</td>
	                <td colspan="2">
	                	${tu.format(paramDao.getParamDateTime(process.id, PROCESS_PARAM_TIME_FROM), 'ymdhm')} -
	                	${tu.format(paramDao.getParamDateTime(process.id, PROCESS_PARAM_TIME_TO), 'ymdhm')}
	                </td>
	            </tr>
	            <tr>
	                <td>Список исполнителей</td>
	                <td colspan="5">${u.getObjectTitles(ctxUserList, process.getExecutorIds())}</td>
	            </tr>
	            <tr>
	                <td>Примечание</td>
	                <td colspan="5">${process.description}</td>
	            </tr>
	            <tr>
	                <td>Информация по адресу</td>
	                <td colspan="5">${addr.house.comment}</td>
	            </tr>
	        </table>
	     </u:sc>

	     <c:set var="executors" value="${process.getExecutorIds()}"/>
   </c:forEach>
</body>
</html>