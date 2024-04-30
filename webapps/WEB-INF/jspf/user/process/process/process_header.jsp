<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="priority" value="${process.priority}"/>
	<%@ include file="/WEB-INF/jspf/process_color.jsp"%>

	<c:set var="uiid" value="${u:uiid()}"/>
	<div style="background-color: ${color}; padding-bottom: 0;" class="box">
		<div id="${uiid}" class="pb05">
			<ui:when type="user">
				<c:set var="cloneAllowed" value="${ctxUser.checkPerm('ru.bgcrm.struts.action.ProcessAction:processClone')}"/>
				<c:set var="mergeAllowed" value="${ctxUser.checkPerm('ru.bgcrm.struts.action.ProcessAction:processMerge')}"/>
				<c:set var="deleteAllowed" value="${ctxUser.checkPerm('ru.bgcrm.struts.action.ProcessAction:processDelete')}"/>

				<c:if test="${cloneAllowed || mergeAllowed || deleteAllowed}">
					<c:set var="uiidMenu" value="${u:uiid()}"/>
					<ui:popup-menu id="${uiidMenu}">
						<c:if test="${cloneAllowed}">
							<c:url var="url" value="${form.httpRequestURI}">
								<c:param name="action" value="processClone"/>
								<c:param name="id" value="${process.id}"/>
							</c:url>
							<li>
								<a href="#" onclick="if (confirm('${l.l('Clone process')}?'))
									$$.ajax.post('${url}').done((response) => {
										$$.process.open(response.data.process.id);
									}); return false;">
									<i class="ti-layers"></i>
									${l.l('Clone process')}
								</a>
							</li>
						</c:if>

						<c:if test="${mergeAllowed}">
							<c:url var="url" value="/user/empty.do">
								<c:param name="returnUrl" value="${requestUrl}"/>
								<c:param name="returnChildUiid" value="${tableId}"/>
								<c:param name="id" value="${process.id}"/>
								<c:param name="forwardFile" value="/WEB-INF/jspf/user/process/process/editor_merge.jsp"/>
							</c:url>
							<li>
								<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}')); return false;">
									<i class="ti-shift-right"></i>
									${l.l('Слить в существующий')}
								</a>
							</li>
						</c:if>

						<c:if test="${deleteAllowed}">
							<c:url var="url" value="${form.httpRequestURI}">
								<c:param name="action" value="processDelete"/>
								<c:param name="id" value="${process.id}"/>
							</c:url>
							<li>
								<a href="#" onclick="if (confirm('${l.l('Удалить процесс')}?')) $$.ajax.post('${url}').done(() => { ${returnBreakCommand} }); return false;">
									<i class="ti-trash"></i>
									${l.l('Удалить процесс')}
								</a>
							</li>
						</c:if>
					</ui:popup-menu>

					<c:set var="uiidMenuLink" value="${u:uiid()}"/>
					[<a href="#" id="${uiidMenuLink}">...</a>]
					<script>
						$(() => {
							$$.ui.menuInit($("#${uiidMenuLink}"), $("#${uiidMenu}"), "left");
						})
					</script>
				</c:if>
			</ui:when>

			<%@ include file="process_header_type.jsp"%>

			<ui:when type="user">
				(<ui:process-link id="${process.id}"/>)

				<p:check action="ru.bgcrm.struts.action.ProcessAction:processPriorityUpdate">
					<c:if test="${processType.properties.configMap['hidePriority'] ne 1}">
						<c:url var="url" value="/user/empty.do">
							<c:param name="returnUrl" value="${requestUrl}"/>
							<c:param name="returnChildUiid" value="${tableId}"/>
							<c:param name="id" value="${process.id}"/>
							<c:param name="forwardFile" value="/WEB-INF/jspf/user/process/process/editor_priority.jsp"/>
							<c:param name="priority" value="${process.priority}"/>
						</c:url>
						[<a href="#" onclick="$$.ajax.load('${url}', $('#${uiid}')); return false;">${l.l('priority')}</a>]
					</c:if>
				</p:check>

				<p:check action="ru.bgcrm.struts.action.ProcessAction:unionLog">
					<c:url var="logUrl" value="${form.httpRequestURI}">
						<c:param name="action" value="unionLog"></c:param>
						<c:param name="id" value="${form.id}"></c:param>
						<c:param name="type" value="process"></c:param>
						<c:param name="objectType" value="${form.param.objectType}"></c:param>
						<c:param name="returnUrl" value="${requestUrl}"></c:param>
					</c:url>
					<c:if test="${not empty processType}">
						[<a href="#" onclick="$$.ajax.load('${logUrl}', $('#${tableId}').parent()); return false;">${l.l('log')}</a>]
					</c:if>
				</p:check>
			</ui:when>
		</div>

		<div class="pb05">
			${l.l('Created')}: ${tu.format( process.createTime, 'ymdhms' )}
			<c:if test="${process.createUserId gt 0}">
				(<ui:user-link id="${process.createUserId}"/>)
			</c:if>
		</div>
		<div class="pb05">
			<%@ include file="process_status_current.jsp"%>
		</div>
		<c:if test="${process.closeUserId gt 0}">
			<div class="pb05">Закрыт: ${tu.format(process.closeTime, 'ymdhms')} (<ui:user-link id="${process.closeUserId}"/>)</div>
		</c:if>
	</div>
</u:sc>