<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="processQueueShow">
	 <html:form action="/usermob/process" styleId="processQueueSelect" styleClass="mb05">
		<input type="hidden" name="action" value="queue"/>
		<c:set var="currentQueueId" value="${form.param.id}"/>

		<c:set var="valuesHtml">
			<c:forEach items="${form.response.data.list}" var="item">
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
		
		<%-- разрешённые к созданию типы --%>
		<c:set var="createAllowedProcessList" value="${queue.createAllowedProcessList}"/>
		
		<c:set var="display"><c:if test="${not empty createAllowedProcessList}">display: table-cell;</c:if></c:set>
		
		<ui:combo-single hiddenName="id" valuesHtml="${valuesHtml}" value="${currentQueueId}"
			prefixText="${l.l('Очередь')}:" style="${display}width: 100%;"
			onSelect="openUrlToParent( formUrl( $hidden[0].form ), $('#processQueueShow') )"/>
		
		<c:if test="${not empty createAllowedProcessList}">
			<div style="${display}; white-space: nowrap;">
				<c:forEach var="type" items="${createAllowedProcessList}">
					<c:url var="createUrl" value="process.do">
						<c:param name="action" value="processCreate"/>
						<c:param name="typeId" value="${type.id}"/>
					</c:url>
					
					<c:set var="showEditor">$('#processQueueShow').hide(); $('#processQueueEditProcess').show();</c:set>
					<c:set var="createCommand">
						var result = sendAJAXCommand( '${createUrl}' ); if( result ){ openUrlTo( 'process.do?wizard=1&id=' + result.data.process.id, $('#processQueueEditProcess') ); ${showEditor} };
					</c:set>
					
					<button type="button" class="btn-green ml1" onclick="${createCommand}">${type.title}</button>
				</c:forEach>
			</div>	
		</c:if>
	</html:form>
	
	<%-- processQueueFilter, невидиый фильтр - он же форма для вывода очереди --%>	
	<html:form action="/usermob/process" styleId="processQueueFilter" style="display: none;">
		<input type="hidden" name="action" value="queueShow"/>
		<input type="hidden" name="id" value="${queue.id}"/>
		<input type="hidden" name="page.pageIndex" value="-1"/>
		<input type="hidden" name="media" value="print"/>
	</html:form>
		
	<div id="processQueueData"></div>
</div>

<script>
	$(function() {
		openUrlTo( formUrl( $('#processQueueShow > #processQueueFilter') ), $('#processQueueData') );
	})
</script>
 

<div id="processQueueEditProcess">
	<%-- здесь открывается мастер редактирования процесса --%>
</div>