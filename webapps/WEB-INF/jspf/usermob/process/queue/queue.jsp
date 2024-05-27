<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="processQueueShow">
	 <html:form action="/usermob/process" styleId="processQueueSelect" styleClass="mb05" style="display: flex;">
		<input type="hidden" name="method" value="queue"/>
		<c:set var="currentQueueId" value="${form.param.id}"/>

		<c:set var="valuesHtml">
			<c:forEach items="${frd.list}" var="item">
				<c:if test="${item.configMap.showIn eq 'usermob'}">
					<c:if test="${empty currentQueueId}">
						<c:set var="currentQueueId" value="${item.id}"/>
					</c:if>
					<li value="${item.id}">${item.title}</li>
				</c:if>
			</c:forEach>
		</c:set>

		<%-- текущая очередь --%>
		<c:set var="queue" value="${ctxProcessQueueMap[u:int(currentQueueId)]}"/>

		<ui:combo-single hiddenName="id" valuesHtml="${valuesHtml}" value="${currentQueueId}"
			prefixText="${l.l('Очередь')}:" style="width: 100%;"
			onSelect="$$.ajax.load($hidden[0].form, $('#processQueueShow').parent())"/>

		<%-- allowed for creation types --%>
		<c:set var="createAllowedProcessList" value="${queue.createAllowedProcessList}"/>
		<c:if test="${not empty createAllowedProcessList}">
			<div style="white-space: nowrap;">
				<c:forEach var="type" items="${createAllowedProcessList}">
					<c:url var="createUrl" value="process.do">
						<c:param name="method" value="processCreate"/>
						<c:param name="typeId" value="${type.id}"/>
					</c:url>

					<c:set var="showEditor">$('#processQueueShow').hide(); $('#processQueueEditProcess').show();</c:set>
					<c:set var="createCommand">
						$$.ajax.post('${createUrl}').done((result) => { $$.ajax.load('process.do?wizard=1&id=' + result.data.process.id, $('#processQueueEditProcess')); ${showEditor} });
					</c:set>

					<button type="button" class="btn-green ml1" onclick="${createCommand}">${type.title}</button>
				</c:forEach>
			</div>
		</c:if>
	</html:form>

	<%-- processQueueFilter, невидиый фильтр - он же форма для вывода очереди --%>
	<html:form action="/usermob/process" styleId="processQueueFilter" style="display: none;">
		<input type="hidden" name="method" value="queueShow"/>
		<input type="hidden" name="id" value="${queue.id}"/>
		<input type="hidden" name="page.pageIndex" value="-1"/>
		<input type="hidden" name="media" value="print"/>
	</html:form>

	<div id="processQueueData"></div>
</div>

<script>
	$$.ajax.load($('#processQueueShow > #processQueueFilter'), $('#processQueueData') );
</script>


<div id="processQueueEditProcess">
	<%-- здесь открывается мастер редактирования процесса --%>
</div>