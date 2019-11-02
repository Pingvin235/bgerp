<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="config" value="${u:getConfig( processor.configMap, 'ru.bgcrm.event.listener.DefaultMarkedProcessor$Config' )}"/> 

<c:forEach var="command" items="${config.commandList}">
	<c:choose>
		<c:when test="${command.name eq 'setStatus'}">
			<div class="pr1" style="display: inline-block;">
				<u:sc>
					<c:set var="hiddenName" value="statusId"/>
					<c:set var="list" value="${ctxProcessStatusList}"/>
					<c:set var="map" value="${ctxProcessStatusMap}"/>
					<c:set var="available" value="${command.allowedIds}"/>
					
					<c:set var="prefixText" value="Статус:"/>
					<c:set var="widthTextValue" value="100px"/>
					<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
				</u:sc>
			</div>
		</c:when>
		<c:when test="${command.name eq 'addExecutors'}">
			<div class="pr1" style="display: inline-block;">
				<u:sc>
					<c:set var="groupListId" value="${u:uiid()}"/>
					<c:set var="executorListId" value="${u:uiid()}"/>
					<c:set var="groupParamName" value="group"/>
					<c:set var="executorParamName" value="executor"/>
					
					<%-- <c:set var="id" value="${groupListId}"/>
					<c:set var="prefixText" value="Группы:"/>
					<c:set var="paramName" value="${groupParamName}"/>
					<c:set var="list" value="${ctxUserGroupList}"/>
					<c:set var="available" value="${command.allowedIds}"/>
					<c:set var="onChange">updateExecutors( $('#${groupListId}'), $('#${executorListId}'), '${groupParamName}', '${executorParamName}' ,'${showEmptyExecutor}', '');</c:set>
					<%@ include file="/WEB-INF/jspf/combo_check.jsp"%> --%>
				
					<ui:combo-check 
						id="${groupListId}" prefixText="Группы:" paramName="${groupParamName}"
						list="${ctxUserGroupList}" available="${command.allowedIds}"
						onChange="updateExecutors( $('#${groupListId}'), $('#${executorListId}'), '${groupParamName}', '${executorParamName}' ,'${showEmptyExecutor}', '');"/>
				
				
					<%-- <c:set var="id" value="${executorListId}"/>		
					<c:set var="prefixText" value="Исполнители:"/>
					<c:set var="widthTextValue" value="150px"/>
					<c:set var="showFilter" value="1"/>
					<%@ include file="/WEB-INF/jspf/combo_check.jsp"%> --%>
					
					<ui:combo-check
						id="${executorListId}" prefixText="Исполнители:"
						widthTextValue="150px" showFilter="1"/>					
				</u:sc>				
			</div>
		</c:when>
		<c:when test="${command.name eq 'setParam'}">
			<u:sc>
				<%-- загадочная проблема: если переменную тут попытаться назвать param, то она уже есть и её не переопределить.. --%>
				<c:set var="p" value="${ctxParameterMap[command.paramId]}"/>
				<div class="pr1" style="display: inline-block;">
					<c:choose>
						<c:when test="${p.type eq 'date' or p.type eq 'datetime'}">
							${p.title}:
							<ui:date-time parameter="${p}" paramName="param${p.id}" type="${p.configMap.type}"/>
						</c:when>
						<c:otherwise>
							Параметры данного типа пока не поддержаны.
						</c:otherwise>
					</c:choose>							
				</div>
			</u:sc>
		</c:when>
	</c:choose>		
</c:forEach>