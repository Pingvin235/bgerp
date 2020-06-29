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

					<ui:combo-check
						id="${groupListId}" prefixText="Группы:" paramName="${groupParamName}"
						list="${ctxUserGroupList}" available="${command.allowedIds}"
						onChange="updateExecutors( $('#${groupListId}'), $('#${executorListId}'), '${groupParamName}', '${executorParamName}' ,'${showEmptyExecutor}', '');"/>

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
							<c:set var="dateInputUiid" value="${u:uiid()}"/>
							<input type="text" name="param${p.id}" id="${dateInputUiid}"/>
							<ui:date-time selector="#${dateInputUiid}" parameter="${p}"/>
							<script>
								$(function () {
									$("#${dateInputUiid}").closest("form").on("show", () => {
										$$.ui.inputFocus($("#${dateInputUiid}"));
									});
								});
							</script>
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