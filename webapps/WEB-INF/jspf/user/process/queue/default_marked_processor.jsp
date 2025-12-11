<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="config" value="${processor.configMap.getConfig('ru.bgcrm.event.listener.DefaultMarkedProcessor$Config')}"/>

<div class="in-inline-block in-mr1-all" style="display: inline-block;">
	<c:choose>
		<c:when test="${processor.htmlReport}">
			<div class="bold">${l.l('Select processes in the table for making a report')}</div>
		</c:when>
		<c:otherwise>
			<c:forEach var="command" items="${config.commandList}">
				<c:choose>
					<c:when test="${command.name eq 'setStatus'}">
						<div>
							<u:sc>
								<ui:combo-single
									name="statusId" list="${ctxProcessStatusList}" map="${ctxProcessStatusMap}"
									available="${command.allowedIds}" prefixText="${l.l('Status')}:" widthTextValue="10em"/>
							</u:sc>
						</div>
					</c:when>
					<c:when test="${command.name eq 'addExecutors'}">
						<div>
							<u:sc>
								<c:set var="groupListId" value="${u:uiid()}"/>
								<c:set var="executorListId" value="${u:uiid()}"/>
								<c:set var="groupParamName" value="group"/>
								<c:set var="executorParamName" value="executor"/>

								<ui:combo-check
									id="${groupListId}" prefixText="${l.l('Groups')}:" paramName="${groupParamName}"
									list="${ctxUserGroupList}" available="${command.allowedIds}"
									onChange="updateExecutors( $('#${groupListId}'), $('#${executorListId}'), '${groupParamName}', '${executorParamName}' ,'${showEmptyExecutor}', '');"/>

								<ui:combo-check
									id="${executorListId}" prefixText="${l.l('Executors')}:"
									widthTextValue="12em" showFilter="1"/>
							</u:sc>
						</div>
					</c:when>
					<c:when test="${command.name eq 'setParam'}">
						<u:sc>
							<%-- загадочная проблема: если переменную тут попытаться назвать param, то она уже есть и её не переопределить.. --%>
							<c:set var="p" value="${ctxParameterMap[command.paramId]}"/>
							<div>
								<c:choose>
									<c:when test="${p.type eq 'date' or p.type eq 'datetime'}">
										${p.title}:
										<c:set var="dateInputUiid" value="${u:uiid()}"/>
										<input type="text" name="param${p.id}" id="${dateInputUiid}"/>
										<ui:date-time selector="#${dateInputUiid}" parameter="${p}"/>
										<script>
											$(function () {
												$("#${dateInputUiid}").closest("form").on("show", () => {
													$$.ui.inputFocus("${dateInputUiid}");
												});
											});
										</script>
									</c:when>
									<c:when test="${p.type eq 'list'}">
										<ui:combo-check name="param${p.id}" list="${p.listParamValues}" prefixText="${p.title}:" widthTextValue="12em"/>
									</c:when>
									<c:otherwise>
										${l.l('The parameter\'s type is not supported.')}
									</c:otherwise>
								</c:choose>
							</div>
						</u:sc>
					</c:when>
				</c:choose>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</div>